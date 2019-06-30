#define LOG_TAG "ImageDecode"

#include <securec.h>

#include <android/bitmap.h>
#include <setjmp.h>
#include <SkCanvas.h>
#include <SkColorSpace.h>
#include <SkStream.h>
#ifndef  PLATFORM_VERSION_O
#include <SkImageDecoder.h>
#else
#include <SkCodec.h>
#include <SkEncodedImageFormat.h>
#include <SkAndroidCodec.h>
#endif

#include "image_decode.h"
#include <utils/Log.h>

#ifdef __cplusplus
#if __cplusplus
extern "C"{
#include <jpeglib.h>
#include <jerror.h>
#include <hi_type.h>
#include <stdlib.h>

#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include "cutils/properties.h"

}
#endif
#endif

#ifdef LOG_ENABLE
#define DEBUG_LOG 1
#else
#define DEBUG_LOG 0
#endif
#ifdef PRODUCT_STB
#define WIDTH_4K 4096
#else
#define WIDTH_4K 3840
#endif

#define HEIGHT_4K 2160
#define LENGTH_4K 4096

using namespace android;


jmp_buf  setjmp_buffer;

static HI_VOID DEC_ERR_FUCNTION(j_common_ptr cinfo)
{
    (*cinfo->err->output_message) (cinfo);
    longjmp(setjmp_buffer, 1);
}

#ifndef PLATFORM_VERSION_O		
static SkImageDecoder::Mode getSkDecodeMode(ImageDecoder::Mode mode) {		
    static const SkImageDecoder::Mode md[] = {		
        SkImageDecoder::kDecodeBounds_Mode,//DECODE_BOUNDS 		
        SkImageDecoder::kDecodePixels_Mode,//DECODE_PIXELS 		
    };		
    return md[mode];		
}
#endif

#ifdef PLATFORM_VERSION_KITKAT
int ImageDecoder::setAndroidBitmapInfo(AndroidBitmapInfo *info, ImageDecoder::DecContex *ctx) {
    if(NULL == ctx || NULL == ctx->bitmap) {
        return -1;
    }

    info->width = ctx->outWidth;
    info->height = ctx->outHeight;
    info->stride = ctx->bitmap->rowBytes();
    info->flags  = 0;
    switch (ctx->bitmap->getConfig()) {
        case SkBitmap::kARGB_8888_Config:
            info->format = ANDROID_BITMAP_FORMAT_RGBA_8888;
            break;
        case SkBitmap::kA8_Config:
            info->format = ANDROID_BITMAP_FORMAT_A_8;
            break;
        case SkBitmap::kRGB_565_Config:
            info->format = ANDROID_BITMAP_FORMAT_RGB_565;
            break;
        case SkBitmap::kARGB_4444_Config:
            info->format = ANDROID_BITMAP_FORMAT_RGBA_4444;
            break;
        default :
            info->format = ANDROID_BITMAP_FORMAT_NONE;
            break;
    }
    return 0;
}

#else

int ImageDecoder::setAndroidBitmapInfo(AndroidBitmapInfo *info, ImageDecoder::DecContex *ctx) {
    if(NULL == ctx || NULL == ctx->bitmap) {
        return -1;
    }

    info->width = ctx->outWidth;
    info->height = ctx->outHeight;
    info->stride = ctx->bitmap->rowBytes();
    info->flags  = 0;
    switch (ctx->bitmap->colorType()) {

        case kN32_SkColorType:
            info->format = ANDROID_BITMAP_FORMAT_RGBA_8888;
            break;
        case kRGB_565_SkColorType:
            info->format = ANDROID_BITMAP_FORMAT_RGB_565;
            break;
        case kARGB_4444_SkColorType:
            info->format = ANDROID_BITMAP_FORMAT_RGBA_4444;
            break;
        case kAlpha_8_SkColorType:
            info->format = ANDROID_BITMAP_FORMAT_A_8;
            break;
        default:
            info->format = ANDROID_BITMAP_FORMAT_NONE;
            break;
    }
    return 0;
}
#endif

ImageDecoder::DecContex::DecContex():outWidth(0),outHeight(0),sampleSize(1),decode_mode(ImageDecoder::DECODE_PIXELS),rotateDegree(0),mirror(0) {
    bitmap = new SkBitmap();
}

ImageDecoder::DecContex::~DecContex() {
    delete bitmap;
    bitmap = NULL;
}

#ifdef PLATFORM_VERSION_KITKAT

int ImageDecoder::decodeFile(const char *path, DecContex *opt) {
    if(NULL == opt || NULL == opt->bitmap) {
        ALOGE("ERR: decodeFile DecContex null %d" , __LINE__);
        return -1;
    }

    SkBitmap bm;
    SkFILEStream stream(path);
    if (!stream.isValid()) {
        ALOGE("ERR: SKFILEStream Could not read %d" , __LINE__);
        return -1;
    }

    SkImageDecoder* decoder;
    decoder = SkImageDecoder::Factory(&stream);
    if(NULL == decoder){
        ALOGE("ERR: decodeFile decoder Factory failed. %d" , __LINE__);
        return -1;
    }

    decoder->setSampleSize(opt->sampleSize);

    bool succ = decoder->decode(&stream, &bm, SkBitmap::kARGB_8888_Config, getSkDecodeMode(opt->decode_mode));
    if(!succ) {
        ALOGE("ERR: decodeFile decode failed %d" , __LINE__);
        return -1;
    }
    ALOGI_IF(DEBUG_LOG, "Decode fmt %s info[w %d h %d sample_size %d  config %d], LINE: %d", decoder->getFormatName() , bm.width(), bm.height(), opt->sampleSize, bm.getConfig(), __LINE__);

    opt->outWidth = bm.width();
    opt->outHeight = bm.height();

    if(DECODE_BOUNDS == opt->decode_mode){
        return 0;
    }

    if(bm.width() > LENGTH_4K || bm.height() > LENGTH_4K)
    {
        if(bm.width() > bm.height())
        {
           opt->outWidth = LENGTH_4K;
           opt->outHeight = (LENGTH_4K * bm.height())/bm.width();
        }
        else
        {
           opt->outHeight = LENGTH_4K;
           opt->outWidth = (LENGTH_4K * bm.width())/bm.height();
        }
        ALOGI("++++++++++++++++LINE %d opt->outWidth: %d opt->outHeight: %d  bm.width(): %d bm.height(): %d", __LINE__ ,opt->outWidth, opt->outHeight,bm.width(),bm.height());
    }

    opt->bitmap->setConfig(SkBitmap::kARGB_8888_Config, opt->outWidth, opt->outHeight);
    opt->bitmap->allocPixels();
    if(NULL == opt->bitmap->getPixels()){
        ALOGE("ERR: decodeFile allocPixels fail %d" , __LINE__);
        return -1;
    }

    char galleryl_savepic[PROPERTY_VALUE_MAX] = {0};
    property_get("vendor.higalleryl.savepic", galleryl_savepic, "false");
    if (0 == strcmp(galleryl_savepic, "true")) {
        int fd = -1;
        fd = open("/sdcard/bitmap_draw_beforedraw.data", O_WRONLY | O_CREAT | O_TRUNC, 0664);
        if (fd == -1) {
            ALOGE("shawDebug ERR: create file bitmap.data failed. %d" , __LINE__);
        }
        write(fd, bm.getPixels(), bm.width()*bm.height()*4);
        close(fd);
    }

    SkCanvas canvas(*opt->bitmap);
    SkPaint paint;

    SkRect r;
    SkIRect r_src;
    r.set(0, 0, opt->outWidth, opt->outHeight);
    r_src.set(0, 0, bm.width(), bm.height());
    canvas.drawBitmapRect(bm, &r_src, r, &paint);

    return 0;
}

#else

int BitmapRotate(const int degree, const int *width, const int *height, SkBitmap *fromBmp, SkBitmap *toBmp, const bool mirror)
{
        int newWidth = *width;
        int newHeight = *height;

        SkImageInfo info = SkImageInfo::Make(newWidth, newHeight, kN32_SkColorType, kPremul_SkAlphaType);
        toBmp->setInfo(info);
        toBmp->allocPixels(info);
        if(NULL == toBmp->getPixels()){
            ALOGE("ERR: decodeFile allocPixels fail %d" , __LINE__);
            return -1;
        }
        if (EOK != memset_s(toBmp->getPixels(), toBmp->rowBytes() * toBmp->height(), 0x00, toBmp->rowBytes() * toBmp->height())){
            ALOGE("memset_s failed !");
        }

        SkCanvas canvas(*toBmp);
        SkPaint paint;

        SkRect r;
        SkIRect r_src;
        r.set(0, 0, newWidth, newHeight);
        r_src.set(0, 0, fromBmp->width(), fromBmp->height());
        switch(degree)
        {
            case 0:
            break;

            case 90:
                canvas.translate(SkIntToScalar(newWidth),SkIntToScalar(0));
                canvas.rotate(SkIntToScalar(degree));
                canvas.scale((float)newHeight/(float)newWidth, (float)newWidth/(float)newHeight);
            break;

            case 180:
                canvas.translate(SkIntToScalar(newWidth),SkIntToScalar(newHeight));
                canvas.rotate(SkIntToScalar(degree));
            break;

            case 270:
                canvas.translate(SkIntToScalar(0),SkIntToScalar(newHeight));
                canvas.rotate(SkIntToScalar(degree));
                canvas.scale((float)newHeight/(float)newWidth, (float)newWidth/(float)newHeight);
            break;
        }
#if defined (PLATFORM_VERSION_NOUGAT) || defined (PLATFORM_VERSION_O)
        canvas.drawBitmapRect((const SkBitmap)*fromBmp, (const SkIRect)r_src, r, &paint);
#else
        canvas.drawBitmapRect(*fromBmp, &r_src, r, &paint);
#endif
        if(mirror)
        {
            r.set(0, 0, newWidth, newHeight);
            r_src.set(0, 0, newWidth, newHeight);
            SkMatrix m;
            m.setScale(-1,1);
            m.postTranslate(newWidth, 0);
            canvas.setMatrix(m);
#if defined (PLATFORM_VERSION_NOUGAT) || defined (PLATFORM_VERSION_O)
            canvas.drawBitmapRect(*toBmp, r_src, r, &paint);
#else
            canvas.drawBitmapRect(*toBmp, &r_src, r, &paint);
#endif
        }
        return 0;
}

#ifdef  PLATFORM_VERSION_O
int ImageDecoder::decodeFile(const char *path, DecContex *opt)
{
    int rotateDegree = 0;
    if(NULL == opt || NULL == opt->bitmap) {
        ALOGE("ERR: decodeFile DecContex null %d" , __LINE__);
        return -1;
    }

    rotateDegree = opt->rotateDegree;

    SkBitmap bm;

    std::unique_ptr<SkFILEStream> stream(new SkFILEStream(path));
    if (!stream->isValid()) {
        ALOGE("Resource %s not found.\n", path);
        return -1;
    }

    //std::unique_ptr<SkAndroidCodec> codec(SkAndroidCodec::NewFromStream(stream.release()));
    //if (!codec) {
    //    ALOGE("Unable to create codec '%s'", path);
    //    return -1;
    //}
    // Create the codec.
    //NinePatchPeeker peeker;
    std::unique_ptr<SkAndroidCodec> codec;
    {
        SkCodec::Result result;
        std::unique_ptr<SkCodec> c = SkCodec::MakeFromStream(std::move(stream), &result,
                                                             NULL);
        if (!c) {
            ALOGE("Failed to create image decoder with message '%s'",
                       SkCodec::ResultToString(result));
            return -1;
        }

        codec = SkAndroidCodec::MakeFromCodec(std::move(c));
        if (!codec) {
            return -1;
        }
    }

    SkISize scaledDims_old = codec->getSampledDimensions(1);
    SkImageInfo scaledInfo_old = codec->getInfo()
                .makeWH(scaledDims_old.width(), scaledDims_old.height())
                .makeColorType(kN32_SkColorType);
    bm.setInfo(scaledInfo_old);
    opt->format =codec->getEncodedFormat();
    if(DECODE_PIXELS == opt->decode_mode){
        SkISize scaledDims = codec->getSampledDimensions(opt->sampleSize);
        SkImageInfo scaledInfo = codec->getInfo()
                .makeWH(scaledDims.width(), scaledDims.height())
                .makeColorType(kN32_SkColorType);

        //if SkImageInfo unspecified SkColorSpace, default appoint skcs
        sk_sp<SkColorSpace> skcs = SkColorSpace::MakeRGB(SkColorSpace::kLinear_RenderTargetGamma, SkColorSpace::kSRGB_Gamut);
        SkColorSpaceTransferFn fn;
        if( scaledInfo.colorSpace() && !scaledInfo.colorSpace()->isNumericalTransferFn(&fn)){
            scaledInfo = scaledInfo.makeColorSpace(skcs);
        }

        bm.setInfo(scaledInfo);
        bm.allocPixels();

        SkAndroidCodec::AndroidOptions options;
        options.fSampleSize = opt->sampleSize;
        SkCodec::Result result =
               codec->getAndroidPixels(scaledInfo, bm.getPixels(), bm.rowBytes(), &options);

        if(SkCodec::kSuccess != result) {
            ALOGE("ERR: decodeFile decode failed %d,result %d" , __LINE__,result);
            return -1;
        }
        ALOGI_IF(DEBUG_LOG, "Decode  info[w %d h %d sample_size %d  ctype %d atype %d], LINE: %d",  bm.width(), bm.height(), opt->sampleSize, bm.colorType(), bm.alphaType(), __LINE__);
    }

    char galleryl_savepic[PROPERTY_VALUE_MAX] = {0};
    property_get("vendor.higalleryl.savepic", galleryl_savepic, "false");
    if (0 == strcmp(galleryl_savepic, "true")) {
        int fd = open("/sdcard/decode.data", O_WRONLY | O_CREAT | O_TRUNC, 0664);
        if (fd < 0) {
            ALOGE("Debug ERR: create file bitmap.data failed. %d" , __LINE__);
        }
        int ret = write(fd, bm.getPixels(), bm.rowBytes() * bm.height());
        if (ret < 0) {
            ALOGE("Debug ERR: write bitmap.data failed. %d" , __LINE__);
        }
        close(fd);
    }

    int newWidth = bm.width();
    int newHeight = bm.height();

    if(90 == rotateDegree || 270 == rotateDegree)
    {
        newWidth = bm.height();
        newHeight = bm.width();
    }
    opt->outWidth = newWidth;
    opt->outHeight = newHeight;

    if(DECODE_BOUNDS == opt->decode_mode){
        return 0;
    }

    if(newWidth > WIDTH_4K)
    {
        opt->outWidth = WIDTH_4K;
        opt->outHeight = (WIDTH_4K * newHeight)/newWidth;
        newWidth = WIDTH_4K;
        newHeight = opt->outHeight;
    }
    if(newHeight > HEIGHT_4K)
    {
        opt->outHeight = HEIGHT_4K;
        opt->outWidth = (HEIGHT_4K * newWidth)/newHeight;
    }
    ALOGE("++++++++++++++++LINE %d opt->outWidth: %d opt->outHeight: %d  bm.width(): %d bm.height(): %d", __LINE__ ,opt->outWidth, opt->outHeight,bm.width(),bm.height());
    return BitmapRotate(rotateDegree, &opt->outWidth, &opt->outHeight, &bm, opt->bitmap, opt->mirror);

}
#else
int ImageDecoder::decodeFile(const char *path, DecContex *opt) {
    int rotateDegree = 0;
    if(NULL == opt || NULL == opt->bitmap) {
        ALOGE("ERR: decodeFile DecContex null %d" , __LINE__);
        return -1;
    }

    rotateDegree = opt->rotateDegree;

    SkBitmap bm;
    SkFILEStream stream(path);
    if (!stream.isValid()) {
        ALOGE("ERR: SKFILEStream Could not read %d" , __LINE__);
        return -1;
    }

    SkImageDecoder* decoder;
    decoder = SkImageDecoder::Factory(&stream);
    if(NULL == decoder){
        ALOGE("ERR: decodeFile decoder Factory failed. %d" , __LINE__);
        return -1;
    }

    decoder->setSampleSize(opt->sampleSize);
    bool succ = decoder->decode(&stream, &bm, kN32_SkColorType, getSkDecodeMode(opt->decode_mode));
    if(!succ) {
        ALOGE("ERR: decodeFile decode failed %d" , __LINE__);
        return -1;
    }
    ALOGI_IF(DEBUG_LOG, "Decode fmt %s info[w %d h %d sample_size %d  ctype %d atype %d], LINE: %d", decoder->getFormatName() , bm.width(), bm.height(), opt->sampleSize, bm.colorType(), bm.alphaType(), __LINE__);

    int newWidth = bm.width();
    int newHeight = bm.height();

    if(90 == rotateDegree || 270 == rotateDegree)
    {
        newWidth = bm.height();
        newHeight = bm.width();
    }
    opt->outWidth = newWidth;
    opt->outHeight = newHeight;

    if(DECODE_BOUNDS == opt->decode_mode){
        return 0;
    }

    char galleryl_savepic[PROPERTY_VALUE_MAX] = {0};
    property_get("vendor.higalleryl.savepic", galleryl_savepic, "false");
    if (0 == strcmp(galleryl_savepic, "true")) {
        int fd = -1;
        fd = open("/sdcard/bitmap_draw_beforedraw.data", O_WRONLY | O_CREAT | O_TRUNC, 0664);
        if (fd == -1) {
            ALOGE("Debug ERR: create file bitmap.data failed. %d" , __LINE__);
        }
        int ret = -1;
        ret = write(fd, bm.getPixels(), bm.width()*bm.height()*4);
        if (ret == -1) {
            ALOGE("Debug ERR: write bitmap.data failed. %d" , __LINE__);
        }
        close(fd);
    }

    if(newWidth > WIDTH_4K)
    {
        opt->outWidth = WIDTH_4K;
        opt->outHeight = (WIDTH_4K * newHeight)/newWidth;
        newWidth = WIDTH_4K;
        newHeight = opt->outHeight;
    }
    if(newHeight > HEIGHT_4K)
    {
        opt->outHeight = HEIGHT_4K;
        opt->outWidth = (HEIGHT_4K * newWidth)/newHeight;
    }
    ALOGE("++++++++++++++++LINE %d opt->outWidth: %d opt->outHeight: %d  bm.width(): %d bm.height(): %d", __LINE__ ,opt->outWidth, opt->outHeight,bm.width(),bm.height());
    return BitmapRotate(rotateDegree, &opt->outWidth, &opt->outHeight, &bm, opt->bitmap, opt->mirror);

}
#endif
#endif

static HI_VOID get_jpeg_information(const char *pFileName, bool *bProgressiveMode)
{
    struct jpeg_decompress_struct cinfo;
    jpeg_error_mgr jerr;
    //JPEG_MYERR_S jerr;
    FILE* pInFile = NULL;

    char path[PATH_MAX + 1] = {0x00};
    if( strlen(pFileName) > PATH_MAX || NULL == realpath(pFileName,path))
    {
        return;
    }
    pInFile = fopen(path,"rb");
    if(NULL == pInFile)
    {
        printf("open jpeg file failure");
        return;
    }
    /**
     ** use ourself error manage function
     **/

    cinfo.err = jpeg_std_error(&jerr);
    jerr.error_exit = DEC_ERR_FUCNTION;
    if (setjmp(setjmp_buffer))
    {
        goto DEC_ERROR;
    }
    jpeg_create_decompress(&cinfo);
    jpeg_stdio_src(&cinfo, pInFile);
    jpeg_read_header(&cinfo, TRUE);

    if(cinfo.progressive_mode)
    {
        *bProgressiveMode = true;
    }
    else
    {
        *bProgressiveMode = false;
    }
DEC_ERROR:
    jpeg_destroy_decompress(&cinfo);
    fclose(pInFile);
    //pInFile = NULL;
    return;
}

static int heif_buf_evaluate(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    if(NULL == path) {
        return -1;
    }
    unsigned int bufSize = -1;
    ALOGI_IF(DEBUG_LOG, "(%s) is heif", path);
    //we assume no buf use in docoding heif progress.because heif take up little.
    bufSize = 0;
    return bufSize;
}

static int png_buf_evaluate(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    if(NULL == path) {
        return -1;
    }
    unsigned int bufSize = -1;
    ALOGI_IF(DEBUG_LOG, "(%s) is png", path);
    //we assume no buf use in docoding jpeg baseline progress.
    bufSize = 0;
    return bufSize;
}

static int webp_buf_evaluate(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    if(NULL == path) {
        return -1;
    }
    unsigned int bufSize = -1;
    ALOGI_IF(DEBUG_LOG, "(%s) is webp", path);
    //we assume no buf use in docoding webp progress.
    bufSize = 0;
    return bufSize;
}

static int bmp_buf_evaluate(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    if(NULL == path) {
        return -1;
    }
    ALOGI_IF(DEBUG_LOG, "(%s) is Bmp", path);
    unsigned int bufSize = w * h * 4 + (w/sampleSize) * (h/sampleSize) * 4;
    return bufSize;
}

static int gif_buf_evaluate(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    if(NULL == path) {
        return -1;
    }
    ALOGI_IF(DEBUG_LOG, "(%s) is Gif", path);
    unsigned int bufSize = w * h * 4 + (w/sampleSize) * (h/sampleSize) * 4;
    return bufSize;
}

static int jpeg_buf_evaluate(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    if(NULL == path) {
        return -1;
    }
    unsigned int bufSize = 0;

    bool bProgressive = false;
    get_jpeg_information(path, &bProgressive);

    if(bProgressive) {
        ALOGI_IF(DEBUG_LOG, "(%s) is Jpeg Progressive", path);
        int idct = (int)(((w*1.0f)/(1.0f/8.0f)) * 8 * 8);
        idct *= 3;
        int bufcontroler = (int)((w/8.0f) * (h/8.0f) * 128);
        bufcontroler *= 3;
        int outbuf = (w/sampleSize) * (h/sampleSize) * 4;
        bufSize = idct + bufcontroler + outbuf;
    } else {
        ALOGI_IF(DEBUG_LOG, "(%s) is Jpeg Baseline", path);
        //we assume no buf use in docoding jpeg baseline progress.
        bufSize = w * h * 4 + (w/sampleSize) * (h/sampleSize) * 4;
    }
    return bufSize;
}

#ifdef  PLATFORM_VERSION_O
static int getImageBufferSize(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    unsigned int bufSize;

    static struct buf_cal_t {
        SkEncodedImageFormat  fmt;
        int (*pCal)(const char *path, const int w, const int h, const int sampleSize,ImageDecoder::DecContex *opt );
    } image_size_cal_t[] = {
    #ifdef GOOGLE3
        SkEncodedImageFormat::kUnknown, NULL,
    #endif
        SkEncodedImageFormat::kBMP, bmp_buf_evaluate,
        SkEncodedImageFormat::kGIF, gif_buf_evaluate,
        SkEncodedImageFormat::kICO,NULL,
        SkEncodedImageFormat::kJPEG, jpeg_buf_evaluate,
        SkEncodedImageFormat::kPNG, png_buf_evaluate,
        SkEncodedImageFormat::kWBMP, NULL,
        SkEncodedImageFormat::kWEBP, webp_buf_evaluate,
        SkEncodedImageFormat::kPKM, NULL,
        SkEncodedImageFormat::kKTX, NULL,
        SkEncodedImageFormat::kASTC, NULL,
        SkEncodedImageFormat::kDNG, NULL,
        SkEncodedImageFormat::kHEIF,heif_buf_evaluate,
    };

    SkFILEStream stream(path);
    SkEncodedImageFormat fmt = opt->format;
    ALOGI_IF(DEBUG_LOG, "This  pic  [w %d, h %d, fmt %d]", w, h, (int)fmt);

    if(NULL == image_size_cal_t[(int)fmt].pCal) {
        ALOGE("ERR : Format Do not support %d.", fmt);
        return -1;
    }
    bufSize = image_size_cal_t[(int)fmt].pCal(path, w, h, sampleSize, opt);

    return bufSize;
}

#else
static int getImageBufferSize(const char *path, const int w, const int h, const int sampleSize, ImageDecoder::DecContex *opt) {
    unsigned int bufSize;

    static struct buf_cal_t {
        SkImageDecoder::Format fmt;
        int (*pCal)(const char *path, const int w, const int h, const int sampleSize,ImageDecoder::DecContex *opt );
    } image_size_cal_t[] = {
        SkImageDecoder::kUnknown_Format, NULL,
        SkImageDecoder::kBMP_Format, bmp_buf_evaluate,
        SkImageDecoder::kGIF_Format, gif_buf_evaluate,
        SkImageDecoder::kICO_Format,NULL,
        SkImageDecoder::kJPEG_Format, jpeg_buf_evaluate,
        SkImageDecoder::kPNG_Format, png_buf_evaluate,
        SkImageDecoder::kWBMP_Format, NULL,
        SkImageDecoder::kWEBP_Format, webp_buf_evaluate,
    };

    SkFILEStream stream(path);
    SkImageDecoder::Format fmt = SkImageDecoder::GetStreamFormat(&stream);
    ALOGI_IF(DEBUG_LOG, "Pic %s [w %d, h %d, fmt %d]", path, w, h, (int)fmt);

    if(NULL == image_size_cal_t[(int)fmt].pCal) {
        ALOGE("ERR : Format Do not support %d.", fmt);
        return -1;
    }
    bufSize = image_size_cal_t[(int)fmt].pCal(path, w, h, sampleSize, opt);

    return bufSize;
}
#endif

bool ImageDecoder::decodeSizeEvaluate(const char *path, DecContex *opt, int *usedDecSize) {
    if(NULL == path || NULL == opt) {
        ALOGE("ERR :(%s) LINE %d decodeTest Path/Ctx is Null.", __FUNCTION__, __LINE__);
        return false;
    }

    int width = opt->outWidth;
    int height = opt->outHeight;
    int sampleSize = opt->sampleSize;

    *usedDecSize= getImageBufferSize(path, width, height, sampleSize, opt);
    if(*usedDecSize < 0){
        ALOGE("ERR : Decode Buffer size calculate failed.");
        return false;
    }

    if(*usedDecSize >= maxDecMemSize) {
        ALOGE("ERR : Decode Buffer size is not enough. pic size :%d, Max docode size: %d", *usedDecSize, maxDecMemSize);
        return false;
    }
    ALOGI_IF(DEBUG_LOG, "Decode Buffer size is . pic size :%d, Max docode size: %d", *usedDecSize, maxDecMemSize);
    return true;
}


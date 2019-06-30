#ifndef _IMAGEDECODE_H_
#define _IMAGEDECODE_H_

#include <jni.h>
#include <SkBitmap.h>

#ifdef  PLATFORM_VERSION_O
#include <SkEncodedImageFormat.h>
#endif

namespace android {

#define IMAGE_MAX_W 4000
#define IMAGE_MAX_H 4000
#define IMAGE_MAX_SIZE ((IMAGE_MAX_W) * (IMAGE_MAX_H))

class ImageDecoder {
public:
    enum Mode {
        DECODE_BOUNDS,
        DECODE_PIXELS,
    };

    class DecContex{
    public:
        DecContex();
        ~DecContex();
        int  outWidth;
        int  outHeight;
        int  sampleSize;
        SkBitmap *bitmap;
        Mode decode_mode;
        int rotateDegree;
        bool mirror;
#ifdef  PLATFORM_VERSION_O
        SkEncodedImageFormat format;
#endif
    };

    ImageDecoder(){format=0;maxDecMemSize=0;}
    ~ImageDecoder(){}

    int decodeFile(const char* path, DecContex *opt);
    bool decodeSizeEvaluate(const char* path, DecContex *opt, int *usedDecSize);
    static int setAndroidBitmapInfo(AndroidBitmapInfo *info, ImageDecoder::DecContex *ctx);

    inline void setMaxDecodeSize(int size) {maxDecMemSize = size;}
    inline int  getMaxDecodeSize(){return maxDecMemSize;}
private:
    unsigned int format;
    unsigned int maxDecMemSize;
};//ImageDecoder


JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_BitmapDecodeUtils_nativeDecodeFile
(JNIEnv * env, jobject obj,  jint width, jint height);
JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_BitmapDecodeUtils_decodeSizeEvaluate
(JNIEnv * env, jobject obj, jstring path, jint width, jint height, jint usedDecSize);

};//namespace android
#endif//_IMAGEDECODE_H_


#include <GLES2/gl2.h>
#include <EGL/egl.h>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stddef.h>
#include <android/native_window_jni.h>
#include <android/bitmap.h>
#include <utils/Log.h>
#include <cutils/properties.h>

#include <securec.h>

#include "gallery_impl.h"
#include "OffScreenObject.h"
#include "image_decode.h"

#ifndef PLATFORM_VERSION_KITKAT
#include "libexif/exif-data.h"
#include "libexif/exif-system.h"
#endif

#include <fcntl.h>
#define WIDTH_FULL 480
#define HEIGHT_FULL 270
#define WIDTH_K 1280
#define HEIGHT_K 720
#define WIDTH_2K 1920
#define HEIGHT_2K 1080
#define WIDTH_S4K 3840
#define HEIGHT_S4K 2160
#define WIDTH_4K 4096
#define HEIGHT_4K 2160
#define WIDTH_8K 7680
#define HEIGHT_8K 4320

#define SLIDELENGTH_6K 6144


#define CMD_CANCELED 9 //GalleryCore.CMD_CANCELED
#define CMD_DECODE_OUT_OF_MEMORY_ERROR 11
#define MAX_ITEM 2
int decodeData = 0;
static int original_width = 0,original_height = 0;
static int picRotationDegree = 0;
static bool bRendering = false;
static bool bDeiniting = false;
char *mCurrentPath = NULL;
static bool bGallerylDebug = false;

class StatusSetter{
public:
    bool *to = NULL;
    StatusSetter(bool *to)
    {
        *to = true;
        this->to = to;
    }

    ~StatusSetter()
    {
        this->to = false;
    }
};

using namespace android;

int parse_orientation(const char *filename)
{
  int orientation = 0;

#ifdef PLATFORM_VERSION_KITKAT

  return orientation;

#else

  ExifData *exifData = exif_data_new_from_file(filename);
  if (exifData != NULL) {
    ExifByteOrder byteOrder = exif_data_get_byte_order(exifData);
    ExifEntry *exifEntry = exif_data_get_entry(exifData, EXIF_TAG_ORIENTATION);
    if (exifEntry != NULL)
    {
      orientation = exif_get_short(exifEntry->data, byteOrder);
    }

    exif_data_unref(exifData);
  }

  return orientation;
#endif
}

static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

static void checkEGlError(const char* op) {
    GLint error = eglGetError();
    if (error != EGL_SUCCESS)
        LOGI("after %s() eglGetError (0x%x)\n", op, error);
}

void renderFrame(int type=0);

void waitForRender()
{
    if(bRendering)
    {
        usleep(1000);
    }
}

void scaleAnimation(const int type){
    int duration = gDuration / 2;
    int count = 0;
    int one = 1;
    float rate = 0.5f;
    float offset = 0.5f;
    bool flg = false;
    long time = 0;
    long fpstime = 0;
    struct timeval begintime;
    struct timeval endtime;
    struct timeval fpsbegintime;
    struct timeval fpsendtime;

    gTextureId = &textureArray[0].textureId;
    gMvpMatrix = &textureArray[0].mvpMatrix;
    vVertices = textureArray[0].vVertices;
    gettimeofday(&begintime, NULL);
    while(true){
        if(bDeiniting) return;
        if(bGallerylDebug){
            gettimeofday(&fpsbegintime, NULL);
        }
        count += one;
        esScale(gMvpMatrix, rate, rate, 1.0f);
        if(offset <= 1.0f)
        {
            esScale(&textureArray[1].mvpMatrix, rate, rate, 1.0f);
        }

        renderFrame(type);
        if(bGallerylDebug){
            gettimeofday(&fpsendtime, NULL);
            fpstime = ((fpsendtime.tv_sec - fpsbegintime.tv_sec)*1000000 + (fpsendtime.tv_usec - fpsbegintime.tv_usec)) / 1000;
            LOGI("scaleAnimation form big to small size fpstime =%ld,fps =%f", fpstime, ((float)1000/(float)fpstime));
        }
        if(!flg){
            gettimeofday(&endtime, NULL);
            time = ((endtime.tv_sec - begintime.tv_sec)*1000000 + (endtime.tv_usec - begintime.tv_usec)) / 1000;
            offset = (float)time / (float)duration;
            if(offset > 1.0f){
                if(bGallerylDebug){
                    gettimeofday(&fpsbegintime, NULL);
                }
                gTextureId = &textureArray[1].textureId;
                gMvpMatrix = &textureArray[1].mvpMatrix;
                vVertices = textureArray[1].vVertices;
                count += one;
                esScale(gMvpMatrix, rate, rate, 1.0f);

                renderFrame(type);
                if(bGallerylDebug){
                     gettimeofday(&fpsendtime, NULL);
                     fpstime = ((fpsendtime.tv_sec - fpsbegintime.tv_sec)*1000000 + (fpsendtime.tv_usec - fpsbegintime.tv_usec)) / 1000;
                     LOGI("scaleAnimation form small to big size fpstime =%ld,fps =%f", fpstime, ((float)1000/(float)fpstime));
                }
                rate = 2.0f;
                one = -1;
                flg = true;
            }
        }

        if(count == 0){
            break;
        }
    }
}

void slideAnimation(const int formatType){
    int screenWidth = gScreenWidth;
    int screenHeight = gScreenHeight;
    int stride = -30;
    int add = 0;
    int tmp = 0;
    int flg = 1;
    int type = 0;
    long times = 0;
    long fpstime = 0;
    struct timeval begintime;
    struct timeval endtime;
    struct timeval fpsbegintime;
    struct timeval fpsendtime;

    int r = 0;
    int fd = -1;
    fd = open("/dev/random", O_RDONLY);
    if (fd >= 0)
    {
        int res = read (fd,&r,sizeof (int));
        if (res < 0)
        {
            ALOGE("read animation type fail.");
        }
        close (fd);
    }
    type = r % 4;
    if(type < 0)
    {
        type = 0;
    }
    switch(type){
    case 0:
        flg = -1;
        //no break
    case 1:
        add = abs(stride);
        screenWidth *= flg;
        stride *= flg;
        esTranslate(gMvpMatrix, (GLfloat)screenWidth/(GLfloat)gScreenWidth, 0.0f, 0.0f);
        gettimeofday(&begintime, NULL);
        if(bGallerylDebug){
            gettimeofday(&fpsbegintime, NULL);
        }
        while(true){
            if(bDeiniting) return;
            tmp = add;
            esTranslate(gMvpMatrix, (GLfloat)stride/(GLfloat)gScreenWidth, 0.0f, 0.0f);
            renderFrame(formatType);
            gettimeofday(&endtime, NULL);
            times = ((endtime.tv_sec - begintime.tv_sec)*1000000 + (endtime.tv_usec - begintime.tv_usec)) / 1000;
            add = (int)(abs(screenWidth) * (GLfloat)times / (GLfloat)gDuration);
            stride = (-1) * (add - tmp) * flg;
            if(bGallerylDebug){
                gettimeofday(&fpsendtime, NULL);
                fpstime = ((fpsendtime.tv_sec - fpsbegintime.tv_sec)*1000000 + (fpsendtime.tv_usec - fpsbegintime.tv_usec)) / 1000;
                LOGI("slideAnimation horizontal direction  fpstime =%ld,fps =%f", fpstime, ((float)1000/(float)fpstime));
                gettimeofday(&fpsbegintime, NULL);
            }
            if(add > abs(screenWidth)){
                stride = (-1) * (abs(screenWidth) - tmp) * flg;
                esTranslate(gMvpMatrix, (GLfloat)stride/(GLfloat)gScreenWidth, 0.0f, 0.0f);
                renderFrame(formatType);
                break;
            }
        }
        break;
    case 2:
        flg = -1;
        //no break
    case 3:
        add = abs(stride);
        screenHeight *= flg;
        stride *= flg;
        esTranslate(gMvpMatrix, 0.0f, (GLfloat)screenHeight/(GLfloat)gScreenHeight, 0.0f);
        gettimeofday(&begintime, NULL);
        if(bGallerylDebug){
            gettimeofday(&fpsbegintime, NULL);
        }
        while(true){
            if(bDeiniting) return;
            tmp = add;
            esTranslate(gMvpMatrix, 0.0f, (GLfloat)stride/(GLfloat)gScreenHeight, 0.0f);
            renderFrame(formatType);
            gettimeofday(&endtime, NULL);
            times = ((endtime.tv_sec - begintime.tv_sec)*1000000 + (endtime.tv_usec - begintime.tv_usec)) / 1000;
            add = (int)(abs(screenHeight) * (GLfloat)times / (GLfloat)gDuration);
            stride = (-1) * (add - tmp) * flg;
            if(bGallerylDebug){
                gettimeofday(&fpsendtime, NULL);
                fpstime = ((fpsendtime.tv_sec - fpsbegintime.tv_sec)*1000000 + (fpsendtime.tv_usec - fpsbegintime.tv_usec)) / 1000;
                LOGI("slideAnimation  vertical direction  fpstime =%ld,fps =%f", fpstime, ((float)1000/(float)fpstime));
                gettimeofday(&fpsbegintime, NULL);
            }
            if(add > abs(screenHeight)){
                stride = (-1) * (abs(screenHeight) - tmp) * flg;
                esTranslate(gMvpMatrix, 0.0f, (GLfloat)stride/(GLfloat)gScreenHeight, 0.0f);
                renderFrame(formatType);
                break;
            }
        }
        break;
    default:
        break;
    }
}

void fadeAnimation(const int formatType){
    int duration = gDuration / 2;
    float alpha = 1.0f;
    float offset = 0.1f;
    int flg = 1;
    int index = 0;
    long time = 0;
    long fpstime = 0;
    struct timeval begintime;
    struct timeval endtime;
    struct timeval fpsbegintime;
    struct timeval fpsendtime;
    gTextureId = &textureArray[0].textureId;
    gMvpMatrix = &textureArray[0].mvpMatrix;
    vVertices = textureArray[0].vVertices;
    gettimeofday(&begintime, NULL);
    if(bGallerylDebug){
        gettimeofday(&fpsbegintime, NULL);
    }

    while(true){
        if(bDeiniting) return;
        if(flg){
            alpha = 1.0f - offset;
        }
        else{
            alpha = offset;
        }
        if(alpha < 0.0f){
            alpha = 0.0f;
            flg = 0;
            if(index == 0){
                index = 1;
            }
            else{
                index = 0;
            }
            gTextureId = &textureArray[index].textureId;
            gMvpMatrix = &textureArray[index].mvpMatrix;
            vVertices = textureArray[index].vVertices;
            gettimeofday(&begintime, NULL);
        }
        else if(alpha > 1.0f){
            alpha = 1.0f;
            flg = 1;
        }

        gAlpha = alpha;
        renderFrame(formatType);
        if(alpha == 1.0f){
            break;
        }
        gettimeofday(&endtime, NULL);
        time = ((endtime.tv_sec - begintime.tv_sec)*1000000 + (endtime.tv_usec - begintime.tv_usec)) / 1000;
        offset = (float)time / (float)duration;
        if(bGallerylDebug){
            gettimeofday(&fpsendtime, NULL);
            fpstime = ((fpsendtime.tv_sec - fpsbegintime.tv_sec)*1000000 + (fpsendtime.tv_usec - fpsbegintime.tv_usec)) / 1000;
            LOGI("fadeAnimation timecost %ld ms, fps= %f", fpstime,((float)1000/(float)fpstime));
            gettimeofday(&fpsbegintime, NULL);
        }
    }
}


GLuint createBitmapTexture(GLBitmapInfo *bitmapInfo) {
    // Texture object handle
    GLuint textureId = 0;

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);

    LOGI("createBitmapTexture bitmapInfo.width: %d bitmapInfo.height: %d ,LINE %d ", bitmapInfo->info.width, bitmapInfo->info.height, __LINE__);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmapInfo->info.width, bitmapInfo->info.height, 0,
            GL_RGBA, GL_UNSIGNED_BYTE, bitmapInfo->pixels);
    checkGlError("glTexImage2D");

    //LOGI("to deal with smooth");

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    return textureId;
}

void renderFrame(const int type) {
    struct timeval begintime;
    struct timeval endtime;

    if(bDeiniting) return;

    GLuint texture = osd->getOffScreenTex();
    LOGI("Bind texture : %d to fbo, LINE %d ", texture , __LINE__);

    glBindFramebuffer(GL_FRAMEBUFFER, gFbo);
    glActiveTexture(osd->getOffScreenTexName());
    checkGlError("glActiveTexture");
    //glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gTexture, 0);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
    checkGlError("glFramebufferTexture2D");

    GLenum fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    LOGI("Check fboStatus : %x  %d, LINE: %d", fboStatus, fboStatus, __LINE__);

    GLushort indices[] = { 0, 1, 2, 0, 2, 3 };

    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    checkGlError("glClearColor");
    glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    checkGlError("glClear");

    glUseProgram(gProgram);
    checkGlError("glUseProgram");

    glVertexAttribPointer(gPositionLoc, 3, GL_FLOAT,
            GL_FALSE, 5 * sizeof(GLfloat), vVertices );
    glVertexAttribPointer(gTexCoordLoc, 2, GL_FLOAT,
            GL_FALSE, 5 * sizeof(GLfloat), &vVertices[3]);

    glEnableVertexAttribArray(gPositionLoc);
    glEnableVertexAttribArray(gTexCoordLoc);

    glUniform4f (gDrawColorLoc, 1.0f, 1.0f, 1.0f, gAlpha );
    // Blend particles
    glEnable (GL_BLEND);
    glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, *gTextureId);

    glUniform1i (gSamplerLoc, 0 );

    // Load the MVP matrix
    glUniformMatrix4fv (gMvpLoc, 1, GL_FALSE, ( GLfloat * )&gMvpMatrix->m[0][0]);

    glDrawElements ( GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices );

    if(bGallerylDebug){
        gettimeofday(&begintime, NULL);
    }

    osd->swap(type);

    if(bGallerylDebug){
        gettimeofday(&endtime, NULL);
        long time = ((endtime.tv_sec - begintime.tv_sec)*1000000 + (endtime.tv_usec - begintime.tv_usec)) / 1000;
        LOGI("swap buffer  timecost %ld ms", time);
    }
}

bool setupGraphics(const int w, const int h) {
    const char vertexShader[] =
            "uniform mat4 u_mvpMatrix;                   \n"
            "attribute vec4 a_position;                  \n"
            "attribute vec2 a_texCoord;                  \n"
            "varying vec2 v_texCoord;                    \n"
            "void main()                                 \n"
            "{                                           \n"
            "   gl_Position = u_mvpMatrix * a_position;  \n"
            "   v_texCoord = a_texCoord;                 \n"
            "}                                           \n";

    const char fragmentShader[] =
            "precision mediump float;                            \n"
            "varying vec2 v_texCoord;                            \n"
            "uniform vec4 u_color;                               \n"
            "uniform sampler2D s_texture;                        \n"
            "void main()                                         \n"
            "{                                                   \n"
            "  gl_FragColor = texture2D( s_texture, v_texCoord );\n"
            "  gl_FragColor *= u_color;                          \n"
            "}                                                   \n";

    LOGI("Gallery Version 3.0");
    printGLString("Version", GL_VERSION);
    printGLString("Vendor", GL_VENDOR);
    printGLString("Renderer", GL_RENDERER);
    printGLString("Extensions", GL_EXTENSIONS);

    LOGI("setupGraphics(%d, %d)", w, h);
    gProgram = esLoadProgram(vertexShader, fragmentShader);
    if (!gProgram) {
        LOGE("Could not create program.");
        return GL_FALSE;
    }

    gPositionLoc = glGetAttribLocation(gProgram, "a_position");
    gTexCoordLoc = glGetAttribLocation(gProgram, "a_texCoord");
    gSamplerLoc = glGetUniformLocation(gProgram, "s_texture");
    gMvpLoc = glGetUniformLocation(gProgram, "u_mvpMatrix" );
    gDrawColorLoc = glGetUniformLocation(gProgram, "u_color");

    gScreenWidth = w;
    gScreenHeight = h;
    glViewport(0, 0, w, h);
    checkGlError("glViewport");
    glGenFramebuffers(1, &gFbo);
    checkGlError("glGenFramebuffers");

    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    return true;
}

void getType(){
    int  ret = NO_ERR;
    unsigned int type = 0;
    ret = osd->getFormat(&type);
    if(ret != NO_ERR)
    {
        LOGE("get format err (%d)", __LINE__);
    }

//分档720p, 1080p，3840x2160，4096x2160,后续有8k
    if(TYPE_NORMAL_k ==type){
        gType = TYPE_NORMAL_k;
        gScreenWidth = WIDTH_2K;
        gScreenHeight = HEIGHT_2K;
        glViewport(0, 0, WIDTH_K, HEIGHT_K);
        checkGlError("glViewport");
        LOGI("Format is 720p, set glViewport and output size(1280,720)");
    }else if(TYPE_NORMAL_2k ==type){
        gType = TYPE_NORMAL_2k;
        gScreenWidth = WIDTH_2K;
        gScreenHeight = HEIGHT_2K;
        glViewport(0, 0, WIDTH_2K, HEIGHT_2K);
        checkGlError("glViewport");
        LOGI("Format is 1080p, set glViewport and output size(1920,1080)");
    }else if(TYPE_NORMAL_S4k ==type){
        gType = TYPE_NORMAL_S4k;
        gScreenWidth = WIDTH_S4K;
        gScreenHeight = HEIGHT_S4K;
        glViewport(0, 0, WIDTH_S4K, HEIGHT_S4K);
        checkGlError("glViewport");
        LOGI("Format is 2160p, set glViewport and output size(3840,2160)");
    }else if(TYPE_NORMAL_4k ==type){
        gType = TYPE_NORMAL_4k;
        gScreenWidth = WIDTH_4K;
        gScreenHeight = HEIGHT_4K;
        glViewport(0, 0, WIDTH_4K, HEIGHT_4K);
        checkGlError("glViewport");
        LOGI("Format is 4096, donot set glViewport and output size again");
    }else if(TYPE_NORMAL_8k ==type){
        gType = TYPE_NORMAL_8k;
        gScreenWidth = WIDTH_8K;
        gScreenHeight = HEIGHT_8K;
        glViewport(0, 0, WIDTH_8K, HEIGHT_8K);
        checkGlError("glViewport");
        LOGI("Format is 8k, donot set glViewport and output size again");
    }else{
        gType = TYPE_NORMAL_4k;
        gScreenWidth = WIDTH_4K;
        gScreenHeight = HEIGHT_4K;
        glViewport(0, 0, WIDTH_4K, HEIGHT_4K);
        checkGlError("glViewport");
        LOGI("default Format is 4096, donot set glViewport and output size again");
    }

}

bool initVideo(JNIEnv * env, jobject obj, const int w, const int h, const bool enableDebug, const bool enablePQ) {
    osd = new OffScreenObject();
    int ret = NO_ERR;
#ifdef PRODUCT_STB
    ret = osd->init(w,h, enableDebug, enablePQ);
#else
    ret = osd->init(env, obj,w,h, enableDebug, enablePQ);
#endif
    if(NO_ERR != ret) {
        ALOGE("initVideo failed [%s:%d]", __func__, __LINE__);
        return false;
    }
    setupGraphics(w, h);
    LOGI("initVideo ok");

    return true;
}

#ifdef PRODUCT_STB
int highestOneBit(unsigned int i) {
     // HD, Figure 3-1
      i |= (i >>  1);
      i |= (i >>  2);
      i |= (i >>  4);
      i |= (i >>  8);
      i |= (i >> 16);
      return i - (i >>1);
 }


int computeSampleSizeLarger(const int w, const int h, const int minSideLength) {
        int initialSize =((w / minSideLength)>( h / minSideLength))?(w / minSideLength)
            :( h / minSideLength);
        if (initialSize <= 1) {
            return 1;
        }
         return (initialSize <= 8) ? highestOneBit(initialSize)
                                  : (initialSize / 8 * 8);
}
#endif

void computeUnit(const int width, const int height){
    gWScale = 1.0f;
    gHScale = 1.0f;
    gSampleSize = 1;
    unsigned int imageSide = width > height ? width : height;
#ifdef  PRODUCT_STB
    if((TYPE_NORMAL_k == gType) || (TYPE_NORMAL_2k == gType)){
        gSampleSize = computeSampleSizeLarger(width, height, WIDTH_K);
    }else{
        unsigned int maxSideLength = SLIDELENGTH_6K;
        while(imageSide > maxSideLength) {
            imageSide >>= 1;
            gSampleSize <<= 1;
        }
    }

    int w = (0 == gSampleSize) ? (width) :
        ( (width + gSampleSize - 1L) / (gSampleSize) );
    int h = (0 == gSampleSize) ? (height) :
        ( (height + gSampleSize - 1L) / (gSampleSize) );

    if(gViewMode == ORIGINAL_MODE){
        gFullScreen = false;
    }
    else if(gViewMode == FULLSCREEN_MODE){
        gFullScreen = true;
    }
    else if(gViewMode == AUTO_MODE){
            if(w <= WIDTH_FULL && h <= HEIGHT_FULL){ // <480P double size to show
                gFullScreen = false;
                gScaleSize = true;
                gWScale = 1.0*gScreenWidth/WIDTH_FULL;
                gHScale = 1.0*gScreenHeight/HEIGHT_FULL;
            }else{                      // >480P full screen to show
                gFullScreen = true;
            }
    }
    else if(gViewMode == SCALE_MODE){
        gViewMode = SCALE_MODE;
    }
    else{
        gFullScreen = true;
    }
#else
    unsigned int maxSideLength = SLIDELENGTH_6K;
    if(TYPE_NORMAL_8k ==gType){
        while(imageSide > 2*maxSideLength) {
            imageSide >>= 1;
            gSampleSize <<= 1;
        }
    }else if(TYPE_NORMAL_2k == gType || TYPE_NORMAL_k == gType) {
        while(imageSide >= 2*gScreenWidth) {
            imageSide >>= 1;
            gSampleSize <<= 1;
        }
    }else{
        while(imageSide > maxSideLength) {
            imageSide >>= 1;
            gSampleSize <<= 1;
        }
    }
    int w = width/gSampleSize;
    int h = height/gSampleSize;

    if(gViewMode == ORIGINAL_MODE){
        gFullScreen = false;
    }
    else if(gViewMode == FULLSCREEN_MODE){
        gFullScreen = true;
    }
    else if(gViewMode == AUTO_MODE){
        if (TYPE_NORMAL_k == gType) {
            if (w < 1280 && h < 720) {
                gFullScreen = false;
                gScaleSize = true;
                gWScale = gWRatio;
                gHScale = gHRatio;
            } else {
                gFullScreen = true;
            }
        } else if (TYPE_NORMAL_2k == gType){
            if(w < 1920 && h < 1080){ // <480P double size to show
                gFullScreen = false;
                gScaleSize = true;
                gWScale = gWRatio;
                gHScale = gHRatio;
            }
            else{                      // >480P full screen to show
                gFullScreen = true;
            }
        }else if (TYPE_NORMAL_S4k == gType){
            if(w <= 1920 && h <= 1080){ // <1080P double size to show
                gFullScreen = false;
                gScaleSize = true;
                gWScale = gWRatio;
                gHScale = gHRatio;
            }
            else{                      // >1080P full screen to show
                gFullScreen = true;
            }
        }else if (TYPE_NORMAL_8k == gType){
            if(w <= 3840 && h <= 2160){ // <2160P double size to show
                gFullScreen = false;
                gScaleSize = true;
                gWScale = gWRatio;
                gHScale = gHRatio;
            }
            else{                      // >2160P full screen to show
                gFullScreen = true;
            }
        }
    }
    else if(gViewMode == SCALE_MODE){
        gViewMode = SCALE_MODE;
    }
    else{
        gFullScreen = true;
    }

#endif
}



bool init(const int width, const int height) {
    EGLConfig config;
    EGLint majorVersion;
    EGLint minorVersion;
    EGLSurface eglSurface;
    EGLContext eglContext;

    EGLDisplay eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);

    if (eglDisplay == EGL_NO_DISPLAY) {
        LOGI("init EGL_NO_DISPLAY");
        return GL_FALSE;
    }

    // Initialize EGL
    if (!eglInitialize(eglDisplay, &majorVersion, &minorVersion)) {
        LOGI("init eglInitialize failed err = 0x%x", eglGetError());
        return GL_FALSE;
    }

    EGLint numConfigs = 0;
    EGLint attribList[] = {
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_NONE
    };

    // Choose config
    if (!eglChooseConfig(eglDisplay, attribList, &config, 1, &numConfigs)) {
        LOGI("init eglChooseConfig failed err = 0x%x", eglGetError());
        return GL_FALSE;
    }

    if (numConfigs < 1) {
        LOGI("init no avaliable config err = 0x%x", eglGetError());
        return GL_FALSE;
    }

    eglSurface = eglCreateWindowSurface(eglDisplay, config, mEglNativeWindow, NULL);

    if (eglSurface == EGL_NO_SURFACE) {
        return GL_FALSE;
    }

    EGLint contextAttribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
    eglContext = eglCreateContext(eglDisplay, config, EGL_NO_CONTEXT, contextAttribs);
    if (eglContext == EGL_NO_CONTEXT) {
        checkEGlError("eglCreateContext");
        return GL_FALSE;
    }

    if (!eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
        checkEGlError("eglMakeCurrent");
        return GL_FALSE;
    }

    mEglDisplay = eglDisplay;
    mEglSurface = eglSurface;

    setupGraphics(width, height);

    char galleryl_debug[PROPERTY_VALUE_MAX] = {0};
    property_get("sys.higalleryl.debug", galleryl_debug, "false");
    if (0 == strcmp(galleryl_debug, "true")) {
        bGallerylDebug = true;
    }

    return GL_TRUE;
}

jmethodID mid1;

void notifyFrameChanged(JNIEnv * env, jobject obj) {
    int x_a = (vVertices[0] *  gMvpMatrix->m[0][0] + vVertices[1] *  gMvpMatrix->m[1][0] + gMvpMatrix->m[3][0] + 1) / 2 * gScreenWidth;
    int x_b = (vVertices[5] *  gMvpMatrix->m[0][0] + vVertices[6] *  gMvpMatrix->m[1][0] + gMvpMatrix->m[3][0] + 1) / 2 * gScreenWidth;
    int x_c = (vVertices[10] * gMvpMatrix->m[0][0] + vVertices[11] * gMvpMatrix->m[1][0] + gMvpMatrix->m[3][0] + 1) / 2 * gScreenWidth;

    int left = x_c <= (x_a <= x_b? x_a : x_b)? x_c : (x_a <= x_b? x_a : x_b);
    int right = x_c >= (x_a >= x_b? x_a : x_b)? x_c : (x_a >= x_b? x_a : x_b);

    int y_a = (vVertices[0] *  gMvpMatrix->m[0][1] + vVertices[1] *  gMvpMatrix->m[1][1] + gMvpMatrix->m[3][1] + 1) / 2 * gScreenHeight;
    int y_b = (vVertices[5] *  gMvpMatrix->m[0][1] + vVertices[6] *  gMvpMatrix->m[1][1] + gMvpMatrix->m[3][1] + 1) / 2 * gScreenHeight;
    int y_c = (vVertices[10] * gMvpMatrix->m[0][1] + vVertices[11] * gMvpMatrix->m[1][1] + gMvpMatrix->m[3][1] + 1) / 2 * gScreenHeight;

    int top = y_c <= (y_a <= y_b? y_a : y_b)? y_c : (y_a <= y_b? y_a : y_b);
    int bottom = y_c >= (y_a >= y_b? y_a : y_b)? y_c : (y_a >= y_b? y_a : y_b);

    jclass clazz = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(clazz, "shownFrameChanged", "(IIII)V");
    env->CallVoidMethod(obj, mid, left, top, right, bottom);
}


JNIEXPORT jint JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeGetDecodeData(){
    return decodeData;
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeInit
(JNIEnv * env, jobject obj,  jint videoLayerWidth, jint videoLayerHeight, jint graphicLayerWidth,
 jint graphicLayerHeight, jint maxUsedMemSizeByte, jboolean enablePQ) {
    jclass clazz = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(clazz, "initCompleted", "(Z)V");
    mid1 = env->GetMethodID(clazz, "shownFrameChanged", "(IIII)V");
    if (graphicLayerHeight == 0 || graphicLayerHeight == 0){
        env->CallVoidMethod(obj, mid, false);
        return;
    }
    gWRatio = (float)videoLayerWidth / (float)graphicLayerWidth;
    gHRatio = (float)videoLayerHeight / (float)graphicLayerHeight;

    char galleryl_debug[PROPERTY_VALUE_MAX] = {0};
    property_get("sys.higalleryl.debug", galleryl_debug, "false");
    if (0 == strcmp(galleryl_debug, "true")) {
        bGallerylDebug = true;
    }

    bool result = initVideo(env, obj, videoLayerWidth, videoLayerHeight, bGallerylDebug, enablePQ);
    getType();

    char ram_level[PROPERTY_VALUE_MAX] = {0};
    property_get("ro.lr.ram_level", ram_level, "normal");

    //512M version: ram_level = critical
    int availableMemSize = 100 * 1024 * 1024;
    if (0 == strcmp(ram_level, "critical") && maxUsedMemSizeByte > availableMemSize) {
        ALOGE("max availableMemSize is %d, can not set maxDecMemSize to be %d",
            availableMemSize, maxUsedMemSizeByte);
        maxDecMemSize = availableMemSize;
    } else {
        maxDecMemSize = maxUsedMemSizeByte;
    }

    env->CallVoidMethod(obj, mid, result);
}

JNIEXPORT int JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeGetFormat
    (JNIEnv * env, jobject obj){
    getType();

    return gType;

}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeDeinit
        (JNIEnv * env, jobject obj) {
    gAnimationType = 0;
    if(glIsTexture(textureArray[0].textureId)){
        glDeleteTextures(1, &textureArray[0].textureId);
    }
    if(glIsTexture(textureArray[1].textureId)){
        glDeleteTextures(1, &textureArray[1].textureId);
    }

    glDeleteProgram(gProgram);
    glDeleteFramebuffers(1, &gFbo);

    // EGL clean up
    if (osd != NULL) {
        osd->deInit();
        delete osd;
        osd = NULL;
    }

    bDeiniting = true;
    waitForRender();
    bDeiniting = false;

}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeScale
        (JNIEnv * env, jobject obj, jfloat scaleX, jfloat scaleY) {
    hisiScale(gMvpMatrix, scaleX, scaleY, 1.0f);
    renderFrame(gType);
    notifyFrameChanged(env, obj);
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeTranslate
        (JNIEnv * env, jobject obj, jint tX, jint tY) {
    // Translate away from the viewer
    int tX_r;
    switch(picRotationDegree)
    {
        case 0:
            tX *= 1;
            tY *= 1;
            break;
        case 90:
            tX_r = tX;
            tX = -1 * tY;
            tY = tX_r;
            break;
        case 180:
            tX *= -1;
            tY *= -1;
            break;
        case 270:
            tX_r = tX;
            tX = tY;
            tY = -1 * tX_r;
            break;
        default:
            break;
    }

    bool flg = hisiTranslate(gMvpMatrix, (GLfloat)tX*2/(GLfloat)gScreenWidth,
            (GLfloat)tY*2/(GLfloat)gScreenHeight, 0.0f);
    if(flg){
        renderFrame(gType);
        notifyFrameChanged(env, obj);
    }
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeSetAlpha
        (JNIEnv * env, jobject obj, jfloat alpha) {
    gAlpha = alpha;
    renderFrame(gType);
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeSetAnimationType
        (JNIEnv * env, jobject obj, jint type, jint duration) {
    gAnimationType = type;
    gDuration = duration;
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeRotate
        (JNIEnv * env, jobject obj, jint degree) {
    picRotationDegree = (picRotationDegree + degree) % 360;

    if (picRotationDegree == 90 || picRotationDegree == 270) {
        computeUnit(original_height, original_width);
    } else {
        computeUnit(original_width, original_height);
    }
    esMatrixLoadIdentity(gMvpMatrix);
    esCalcCoordinate(picRotationDegree, gBitmapInfo.info.width, gBitmapInfo.info.height);
    hisiScale(gMvpMatrix, gWScale, gHScale, 1.0f);
    esRotate(gMvpMatrix, picRotationDegree, 0.0f, 0.0f, 1.0f);
    renderFrame(gType);
    notifyFrameChanged(env, obj);
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeReset
        (JNIEnv * env, jobject obj) {
    gTotalX = 0.0f;
    gTotalY = 0.0f;
    gTransMaxX = 0.0f;
    gTransMaxY = 0.0f;
    gTotalScaleX = 1.0f;
    gTotalScaleY = 1.0f;
    gAlpha = 1.0f;
    gRotationDegree = ROTATION_0;
    picRotationDegree = ROTATION_0;
    gAnimationType = 0;
    gDuration = 500;
    if(gMvpMatrix == NULL){
        return;
    }
    computeUnit(original_width, original_height);
    esMatrixLoadIdentity(gMvpMatrix);
    esCalcCoordinate(ROTATION_0, gBitmapInfo.info.width, gBitmapInfo.info.height);

    hisiScale(gMvpMatrix,gWScale,gHScale, 1.0f);
    renderFrame(gType);
    notifyFrameChanged(env, obj);
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeShowImage
        (JNIEnv * env, jobject obj, jstring string, jint viewmode, jint rotateDegree) {
    StatusSetter ss(&bRendering);
    jclass clazz = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(clazz, "showCompleted", "(ZLjava/lang/String;)V");
    jmethodID midex = env->GetMethodID(clazz, "showCompleted", "(ZLjava/lang/String;Z)V");
    jmethodID midex1 = env->GetMethodID(clazz, "showCompleted", "(ZLjava/lang/String;ZI)V");

    const char *path = env->GetStringUTFChars(string, 0);
    char realPath[PATH_MAX + 1] = {0x00};
    if( strlen(path) > PATH_MAX || NULL == realpath(path,realPath))
    {
        ALOGE("ERR: gallery_core Show bitmap is null | LINE:%d", __LINE__);
        env->CallVoidMethod(obj, mid, GL_FALSE, string);
        return;
    }
    mCurrentPath = (char*)path;
    if(viewmode < 0)//just update user to show picture path
    {
        return;
    }

    struct timeval begintime;
    struct timeval endtime;

    if(bGallerylDebug){
        gettimeofday(&begintime, NULL);
    }

    getType();

    int  ret = NO_ERR;
    LOGI("Show bitmap : %s", path);
    ImageDecoder decoder;
    decoder.setMaxDecodeSize(maxDecMemSize);
    ImageDecoder::DecContex ctx;
    ctx.sampleSize = gSampleSize;
    ctx.decode_mode = ImageDecoder::DECODE_BOUNDS;
    ctx.rotateDegree = rotateDegree;
    ctx.mirror = false;

    int orientation = parse_orientation(path);
    switch(orientation)
    {
        case 2:
            ctx.mirror = true;
            break;
        case 5:
            ctx.mirror = true;
            ctx.rotateDegree += 90;
            break;
        case 6:
            ctx.rotateDegree += 90;
            break;
        case 4:
            ctx.mirror = true;
            ctx.rotateDegree += 180;
            break;
        case 3:
            ctx.rotateDegree += 180;
            break;
        case 7:
            ctx.mirror = true;
            ctx.rotateDegree += 270;
            break;
        case 8:
            ctx.rotateDegree += 270;
            break;
        default:
            break;
    }
    ctx.rotateDegree %= 360;

    decodeData = 0;
    ret = decoder.decodeFile(path, &ctx);
    if(ret) {
        ALOGE("ERR: decodeFile failed. %d ", __LINE__);
        decodeData = 1;
        env->CallVoidMethod(obj, mid, GL_FALSE, string);
        return;
    }

    int w = ctx.outWidth;
    int h = ctx.outHeight;
    original_width = ctx.outWidth;
    original_height = ctx.outHeight;
    LOGI("decodeFile original_width=%d, original_height=%d ", original_width, original_height);
    gViewMode = viewmode;

    computeUnit(original_width, original_height);

    LOGI("Decode after  calculate samplesize::  sampleSize: %d, LINE: %d", gSampleSize, __LINE__);
    ctx.sampleSize = gSampleSize;
    ctx.decode_mode = ImageDecoder::DECODE_PIXELS;

    int usedPicMemSize = 0;
    bool bAllowDecode = decoder.decodeSizeEvaluate(path, &ctx, &usedPicMemSize);

    if(bAllowDecode) {
        ret = decoder.decodeFile(path, &ctx);
        w = ctx.outWidth;
        h = ctx.outHeight;
    } else {
        ALOGE("ERR : Have not enough memory size to decode this pic.");
        decodeData = 2;
        env->CallVoidMethod(obj, midex1, GL_FALSE, string, GL_FALSE, CMD_DECODE_OUT_OF_MEMORY_ERROR/*GalleryCore.CMD_DECODE_OUT_OF_MEMORY_ERROR*/);
        return;
    }

    if(bGallerylDebug){
        gettimeofday(&endtime, NULL);
        long time = ((endtime.tv_sec - begintime.tv_sec)*1000000 + (endtime.tv_usec - begintime.tv_usec)) / 1000;
        LOGI("Decode timecost %ld ms, result = %d", time, ret);
    }

    if(ret) {
        ALOGE("ERR: decodeFile failed. %d ", __LINE__);
        decodeData = 3;
        env->CallVoidMethod(obj, mid, GL_FALSE, string);
        return;
    }
    if (EOK != memset_s(&gBitmapInfo, sizeof(gBitmapInfo), 0x00, sizeof(gBitmapInfo))){
            ALOGE("memset_s failed !");
        }
    ImageDecoder::setAndroidBitmapInfo(&gBitmapInfo.info, &ctx);

    gBitmapInfo.pixels = ctx.bitmap->getPixels();

    //clear old texture
    if((textureArray[0].textureId != textureArray[1].textureId) && \
    glIsTexture(textureArray[0].textureId))
    {
        glDeleteTextures(1, &textureArray[0].textureId);
    }

    //move new to old
    bool oldAnimationTexture = glIsTexture(textureArray[1].textureId);
    if(oldAnimationTexture){//store old texture
        if (EOK != memcpy_s(&textureArray[0], sizeof(TextureArray), &textureArray[1], sizeof(TextureArray))){
            ALOGE("memcpy_s failed !");
        }
    }
    //load new
    gTextureId = &textureArray[1].textureId;
    gMvpMatrix = &textureArray[1].mvpMatrix;
    vVertices = textureArray[1].vVertices;
    *gTextureId = createBitmapTexture(&gBitmapInfo);

    gTotalX = 0.0f;
    gTotalY = 0.0f;
    gTransMaxX = 0.0f;
    gTransMaxY = 0.0f;
    gTotalScaleX = 1.0f;
    gTotalScaleY = 1.0f;
    gAlpha = 1.0f;
    gRotationDegree = ROTATION_0;
    esCalcCoordinate(ROTATION_0, gBitmapInfo.info.width, gBitmapInfo.info.height);

    // Generate a model view matrix to rotate/translate the picture
    esMatrixLoadIdentity(gMvpMatrix);

    hisiScale(gMvpMatrix,gWScale,gHScale, 1.0f);

    //no old now, copy current new to old
    if(!oldAnimationTexture)
    {
        if (EOK != memcpy_s(&textureArray[0], sizeof(TextureArray), &textureArray[1], sizeof(TextureArray))){
            ALOGE("memcpy_s failed !");
        }
    }

    if(0 != strcmp(mCurrentPath, path))//user commit new picture to show
    {
        env->CallVoidMethod(obj, midex1, GL_FALSE, string, GL_FALSE, CMD_CANCELED/*GalleryCore.CMD_CANCELED*/);
        return;
    }

    if(!gAnimationType or !oldAnimationTexture){//no animation or no old texture
        renderFrame(gType);
    }
    else
    {
        // animation
        switch(gAnimationType){
            case 1:
                // scale animation
                scaleAnimation(gType);
                break;
            case 2:
                // slide animation
                slideAnimation(gType);
                break;
            case 3:
                // fade animation
                fadeAnimation(gType);
                break;
            default:
                break;
        }
    }

    if(NO_ERR == ret){
        env->CallVoidMethod(obj, midex, GL_TRUE, string, GL_FALSE);
        notifyFrameChanged(env, obj);
        if(bGallerylDebug){
            gettimeofday(&endtime, NULL);
            long time = ((endtime.tv_sec - begintime.tv_sec)*1000000 + (endtime.tv_usec - begintime.tv_usec)) / 1000;
            LOGI("render frame timecost %ld ms, result = %d", time, ret);
        }
        LOGI("nativeShowImage end");
    }
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeShowBitmap
(JNIEnv * env, jobject obj, jobject bitmap, jboolean fullScreen) {

    getType();

    StatusSetter ss(&bRendering);

    AndroidBitmapInfo info;
    if (EOK != memset_s(&gBitmapInfo, sizeof(gBitmapInfo), 0x00, sizeof(gBitmapInfo))){
            ALOGE("memset_s failed !");
        }
    if (AndroidBitmap_getInfo(env, bitmap, &gBitmapInfo.info) < 0) {
        LOGE("AndroidBitmap_getInfo() failed !");
        return;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, &gBitmapInfo.pixels) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed !");
    }

    if(!gAnimationType){
        if(glIsTexture(textureArray[1].textureId)){
            glDeleteTextures(1, &textureArray[1].textureId);
        }
    }

    if (EOK != memset_s(&textureArray[0], sizeof(TextureArray), 0x00, sizeof(TextureArray))){
            ALOGE("memset_s failed !");
        }
    if(gAnimationType){
        if (EOK != memcpy_s(&textureArray[0], sizeof(TextureArray), &textureArray[1], sizeof(TextureArray))){
            ALOGE("memcpy_s failed !");
        }
        gTextureId = &textureArray[1].textureId;
        gMvpMatrix = &textureArray[1].mvpMatrix;
        vVertices = textureArray[1].vVertices;
    }
    else{
        gTextureId = &textureArray[0].textureId;
        gMvpMatrix = &textureArray[0].mvpMatrix;
        vVertices = textureArray[0].vVertices;
    }

    *gTextureId = createBitmapTexture(&gBitmapInfo);

    gTotalX = 0.0f;
    gTotalY = 0.0f;
    gTransMaxX = 0.0f;
    gTransMaxY = 0.0f;
    gTotalScaleX = 1.0f;
    gTotalScaleY = 1.0f;
    gAlpha = 1.0f;
    gFullScreen = fullScreen;
    gRotationDegree = ROTATION_0;
    esCalcCoordinate(ROTATION_0, gBitmapInfo.info.width, gBitmapInfo.info.height);

    // Generate a model view matrix to rotate/translate the picture
    esMatrixLoadIdentity(gMvpMatrix);

    if(!gAnimationType){
        if (EOK != memset_s(&textureArray[1], sizeof(TextureArray), 0x00, sizeof(TextureArray))){
            ALOGE("memset_s failed !");
        }
        if (EOK != memcpy_s(&textureArray[1], sizeof(TextureArray), &textureArray[0], sizeof(TextureArray))){
            ALOGE("memcpy_s failed !");
        }
        renderFrame(gType);
    }

    // animation
    switch(gAnimationType){
        case 1:
            // scale animation
            scaleAnimation(gType);
            break;
        case 2:
            // slide animation
            slideAnimation(gType);
            break;
        case 3:
            // fade animation
            fadeAnimation(gType);
            break;
        default:
            break;
    }

    int result = AndroidBitmap_unlockPixels(env, bitmap);
    if (result < 0) {
        ALOGE("Cannot unlock bitmap pixels");
    }

    jclass clazz = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(clazz, "showCompleted", "(Z)V");
    env->CallVoidMethod(obj, mid, GL_TRUE);
}

JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeInitWithSurface
(JNIEnv * env, jobject obj, jobject surface, jint width, jint height) {
    mEglNativeWindow = ANativeWindow_fromSurface(env, surface);

    bool result = init(width, height);

    jclass clazz = env->GetObjectClass(obj);
    jmethodID mid = env->GetMethodID(clazz, "initCompleted", "(Z)V");
    env->CallVoidMethod(obj, mid, result);
}

JNIEXPORT jboolean JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeDecodeSizeEvaluate (JNIEnv * env, jobject obj, jstring string, jint width, jint height, jint sampleSize, jint usedDecSize) {
    jclass clazz = env->GetObjectClass(obj);

    const char *path = env->GetStringUTFChars(string, 0);

    char realPath[PATH_MAX + 1] = {0x00};
    if( strlen(path) > PATH_MAX || NULL == realpath(path,realPath))
    {
        return false;
    }

    ImageDecoder decoder;
    decoder.setMaxDecodeSize(maxDecMemSize);

    ImageDecoder::DecContex ctx;
    ctx.outWidth = width;
    ctx.outHeight = height;
    ctx.sampleSize = sampleSize;
    bool result =  decoder.decodeSizeEvaluate(path, &ctx, &usedDecSize);
    return result;
}
JNIEXPORT jint JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeGetBitmapOrientation(JNIEnv * env, jobject obj, jstring string){
    jclass clazz = env->GetObjectClass(obj);
    const char *path = env->GetStringUTFChars(string, 0);

    char realPath[PATH_MAX + 1] = {0x00};
    if( strlen(path) > PATH_MAX || NULL == realpath(path,realPath))
    {
        return 0;
    }

    int orientation = parse_orientation(path);
    return orientation;
}

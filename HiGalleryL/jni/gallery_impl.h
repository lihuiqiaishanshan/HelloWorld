#include <EGL/egl.h>
#include <EGL/eglplatform.h>
#include <GLES2/gl2.h>
#include <jni.h>
#include <sys/cdefs.h>
#include <EGL/eglext.h>
#include <GLES2/gl2ext.h>

#include "OffScreenObject.h"
#include "esUtil.h"

__BEGIN_DECLS
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeInit
        (JNIEnv * env, jobject obj,  jint videoLayerWidth, jint videoLayerHeight, jint graphicLayerWidth,
        jint graphicLayerHeight, jint maxUsedMemSizeByte, jboolean enablePQ);
    JNIEXPORT int JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeGetFormat
    (JNIEnv * env, jobject obj);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeDeinit
        (JNIEnv * env, jobject obj);

    JNIEXPORT jint JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeGetDecodeData();
    JNIEXPORT jint JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeGetErrData();
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeScale
        (JNIEnv * env, jobject obj, jfloat scaleX, jfloat scaleY);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeTranslate
        (JNIEnv * env, jobject obj, jint tX, jint tY);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeSetAlpha
        (JNIEnv * env, jobject obj, jfloat alpha);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeRotate
        (JNIEnv * env, jobject obj, jint degree);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeReset
        (JNIEnv * env, jobject obj);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeShowImage
        (JNIEnv * env, jobject obj, jstring string, jint viewmode, jint rotateDegree);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeShowBitmap
        (JNIEnv * env, jobject obj, jobject bitmap, jboolean fullScreen);
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeSetAnimationType
        (JNIEnv * env, jobject obj, jint type, jint duration);
    JNIEXPORT jboolean JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeDecodeSizeEvaluate
        (JNIEnv * env, jobject obj, jstring string, jint width, jint height, jint sampleSize, jint usedPicMemSize);
    //Maybe remove
    JNIEXPORT void JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeInitWithSurface
        (JNIEnv * env, jobject obj, jobject surface, jint width, jint height);
    JNIEXPORT jint JNICALL Java_com_hisilicon_higallery_core_GalleryImpl_nativeGetBitmapOrientation
        (JNIEnv * env, jobject obj, jstring string);
__END_DECLS


GLuint gProgram;
GLuint gPositionLoc;
GLuint gTexCoordLoc;
GLuint gSamplerLoc;
GLuint *gTextureId;
GLuint gMvpLoc;
GLuint gDrawColorLoc;
GLfloat gAlpha;
GLuint gFbo;
int gAnimationType = 0;
int gDuration = 3000;
static int maxDecMemSize = 0;

bool gScaleSize = false;
int gType = 0;
unsigned int gSampleSize =1;
float gWScale=1.0f;
float gHScale=1.0f;
float gWRatio = 1.0f;
float gHRatio = 1.0f;

EGLSurface mEglSurface;
EGLDisplay mEglDisplay;
EGLNativeWindowType  mEglNativeWindow;
static ESMatrix *gMvpMatrix;

android::OffScreenObject *osd = NULL;

static struct GLBitmapInfo{
    void* pixels;
    AndroidBitmapInfo info;
} gBitmapInfo;

typedef struct{
    GLuint textureId;
    GLfloat vVertices[20];
    ESMatrix mvpMatrix;
} TextureArray;

TextureArray textureArray[2];

void notifyFrameChanged();

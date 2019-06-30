#define LOG_TAG "HiGalleryL_OffScreenObject"

#include <sys/mman.h>

#include <linux/ion.h>

#include <securec.h>

#include "OffScreenObject.h"
//#include "VideoBuffer.h"
#include <utils/Log.h>
//test
#include <SkImageEncoder.h>
#include <SkBitmap.h>
#include <SkData.h>
#include <SkStream.h>
#include <cutils/properties.h>
#include <WorkerThread.h>
///
#include "gralloc_priv.h"
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include "cutils/properties.h"
#include <vendor/hisilicon/hardware/hwgraphics/1.0/IGraphics.h>
#include <vendor/hisilicon/hardware/hwgraphics/1.0/types.h>

//using ::vendor::hisilicon::hardware::hwgalleryl::V1_0;
//::Status
using ::vendor::hisilicon::hardware::hwgraphics::V1_0::IGraphics;

int getGPUBufferPhy(struct private_handle_t* handle);
#define GRALLOC_USAGE_PHYSICAL_MEM  (GRALLOC_USAGE_PRIVATE_3) //Alloc physical memory


#define SCREEN_W 1920
#define SCREEN_H 1080
#define HI_SUCCESS 0

static int DEBUG_LOG =0;
static android::sp<IGraphics> mHal = NULL;

using namespace android;

static void checkEglError(const char* op, EGLBoolean returnVal = EGL_TRUE) {
    for (EGLint error = eglGetError(); error != EGL_SUCCESS; error
            = eglGetError()) {
        ALOGE("checkEglError: after %s() (0x%x)", op, error);
    }
}
void *writeAfterBitmap_thread(void *args)
{
    writeBitmap_handle_s  *writeBitmap;
    writeBitmap = (writeBitmap_handle_s*)args;
    int w = writeBitmap->w;
    int h = writeBitmap->h;
    unsigned char *buf = NULL;
    buf = writeBitmap->mBuf;
    ALOGD_IF(DEBUG_LOG, "[%s:%d] [Debug] start to capture decode data" , __FUNCTION__, __LINE__);
    int fd = -1;
    fd = open("/sdcard/bitmap_draw_afterdraw.data", O_WRONLY | O_CREAT | O_TRUNC, 0664);
    if (fd == -1) {
        ALOGE("[%s:%d] Debug ERR: create file bitmap.data failed!", __FUNCTION__, __LINE__);
    }else{
        int ret = -1;
        ret = write(fd, buf, w * h * 4);
        if (ret == -1) {
            ALOGE("[%s:%d] Debug ERR: write file bitmap failed.", __FUNCTION__, __LINE__);
        }
        close(fd);
    }
    return NULL;
}


OffScreenObject::OffScreenObject()
    :bufCount(0) ,mWidth(0) ,mHeight(0), mIsInitialized(false)
    ,mDpy(NULL), mSur(NULL), mCtx(NULL),isAptUsed(false) {
    worker_thread_init();
    for(int i=0;i<NUMOUTPUTBUFFER;i++){
        hOffScreen[i].isAptUsed = false;
        hOffScreen[i].hVidScreen = NULL;
        hOffScreen[i].hAptScreen = NULL;
    }
    writeBitmap.w = 0;
    writeBitmap.h = 0;
    writeBitmap.mBuf = NULL;
    if (mHal == NULL) {
        mHal = IGraphics::getService();
    }
    if (mHal == NULL) {
        ALOGW("IGraphics getService failed!");
    }
}

OffScreenObject::~OffScreenObject() {
    deInit();
}

int OffScreenObject::createEGLContext(const int surfaceW, const int surfaceH) {
    EGLint majorVersion, minorVersion;
    EGLint dspConfigAttribs[] = {
        EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_NONE
    };
    EGLConfig dspConfig = {0};
    EGLint numConfig = -1;

    mDpy = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if(EGL_NO_DISPLAY == mDpy) {
        ALOGE("[%s:%d] ERR: eglGetDisplay failed!", __FUNCTION__, __LINE__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "[%s:%d] eglGetDisplay ok.", __FUNCTION__, __LINE__);

    int ret = eglInitialize(mDpy, &majorVersion, &minorVersion);
    if(EGL_TRUE != ret) {
        ALOGE("[%s:%d] ERR: eglInitialize failed!", __FUNCTION__, __LINE__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "[%s:%d] eglInitialize ok.", __FUNCTION__, __LINE__);

    ret = eglChooseConfig(mDpy, dspConfigAttribs, &dspConfig, 1, &numConfig);

    EGLint pbufferAttribs[] = {
        EGL_WIDTH, surfaceW,
        EGL_HEIGHT, surfaceH,
        EGL_NONE
    };

    mSur = eglCreatePbufferSurface(mDpy, dspConfig, pbufferAttribs);
    if(EGL_NO_SURFACE == mSur) {
        ALOGE("[%s:%d] ERR: EGL No Surface", __FUNCTION__, __LINE__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "[%s:%d] eglCreatePbufferSurface ok. ", __FUNCTION__, __LINE__);

    EGLint contextAttribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
    mCtx = eglCreateContext(mDpy, dspConfig, EGL_NO_CONTEXT, contextAttribs);
    if(EGL_NO_CONTEXT == mCtx) {
        ALOGE("[%s:%d] ERR: eglCreateContext failed!", __FUNCTION__, __LINE__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "[%s:%d] eglCreateContext ok.", __FUNCTION__, __LINE__);

    ret = eglMakeCurrent(mDpy, mSur, mSur, mCtx);
    if(EGL_TRUE != ret) {
        ALOGE("[%s:%d] ERR: EGL eglMakeCurrent failed!", __FUNCTION__, __LINE__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "[%s:%d] eglMakeCurrent to PBuffer ok.", __FUNCTION__, __LINE__);

    eglQuerySurface(mDpy, mSur, EGL_WIDTH, &mWidth);
    eglQuerySurface(mDpy, mSur, EGL_HEIGHT, &mHeight);
    ALOGI_IF(DEBUG_LOG, "[%s:%d] eglQuerySurface size[%d, %d]",  __FUNCTION__, __LINE__, mWidth, mHeight);

    return ret;
}

int OffScreenObject::destroyEGLContext() {
    int ret = EGL_TRUE;
    ALOGI_IF(DEBUG_LOG, "destroyEGLContext enter. ");
    if(EGL_NO_DISPLAY == mDpy) {
        ALOGE("[%s:%d] ERR: EGLDisplay already destroied!", __FUNCTION__, __LINE__);
        return DESTORY_CTX_ERR;
    }

    ret = eglMakeCurrent(mDpy, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    if(EGL_TRUE != ret) {
        ALOGE("[%s:%d] ERR: EGL eglMakeCurrent failed!", __FUNCTION__, __LINE__);
    }

    if(EGL_NO_SURFACE != mSur) {
        eglDestroySurface(mDpy, mSur);
        mSur = EGL_NO_SURFACE;
    }
    ALOGI_IF(DEBUG_LOG, "eglDestroySurface ok.");
    if(EGL_NO_CONTEXT != mCtx) {
        eglDestroyContext(mDpy, mCtx);
        mCtx = EGL_NO_CONTEXT;
    }
    ret = eglTerminate(mDpy);
    if(EGL_TRUE != ret) {
        ALOGE("[%s:%d] ERR: EGL eglTerminate failed!", __FUNCTION__, __LINE__);
    }
    mDpy = EGL_NO_DISPLAY;
    ALOGI_IF(DEBUG_LOG, "destroyEGLContext end.");
    return NO_ERR;
}

int OffScreenObject::createVideoScreen(video_screen_handle_s *vScr, const int w, const int h, bool allocMem) {

    if(NULL != vScr->v_addr && 0 == vScr->phy_addr) {
        ALOGE("[%s:%d] ERR: video screen handle has been created!", __FUNCTION__, __LINE__);
        return VID_SCR_ERR;
    }

    ALOGI_IF(DEBUG_LOG, "createVideoScreen enter.");

    int map_fd = -1;
    int fd = -1;
    int size = w*h;

    vScr->fd = -1;
    vScr->mapfd = map_fd;
    vScr->w = w;
    vScr->h = h;
    vScr->stride = vScr->phy_size / h;
    vScr->size = size;
    ALOGI_IF(DEBUG_LOG, "createVideoScreen ok. ion_fd: %d", fd);
    return NO_ERR;
}


int OffScreenObject::destroyVideoScreen(video_screen_handle_s *vScr) {

    if(NULL == vScr) {
        return VID_SCR_ERR;
    }

    ALOGI_IF(DEBUG_LOG, "destroy video screen handle.LINE: %d " , __LINE__);

    if(NULL != vScr->handle)
    {
        int ret = 0;//ion_free(vScr->fd, vScr->handle);
        if(ret) {
            //ALOGE("ERR: destroy video screen failed .fd %d, handle %d", vScr->fd, vScr->handle);
            ALOGE("ERR: destroy video screen failed .fd %d", vScr->fd);
        }
    }

    return NO_ERR;
}

int OffScreenObject::createAdapterScreen(adapter_screen_handle_s *aScr, const int w, const int h) {
    if(NULL == aScr || NULL == aScr->ost){
        ALOGE("[%s:%d] ERR: Adapter Screen handle has not been created!", __FUNCTION__, __LINE__);
        return APT_SCR_ERR;
    }

    ALOGI_IF(DEBUG_LOG, "createAdapterBuffer enter.");

    aScr->outBuf = new GraphicBuffer(w, h, HAL_PIXEL_FORMAT_BGRA_8888, GraphicBuffer::USAGE_HW_TEXTURE | GraphicBuffer::USAGE_HW_2D | GraphicBuffer::USAGE_SW_READ_OFTEN | GraphicBuffer::USAGE_SW_WRITE_OFTEN | GRALLOC_USAGE_PHYSICAL_MEM);
    if(NULL == aScr->outBuf->getNativeBuffer()){
        ALOGE("[%s:%d] ERR: GraphicBuffer getNativeBuffer failed!", __FUNCTION__, __LINE__);
        return APT_SCR_ERR;
    }

    EGLClientBuffer Clientbuf = (EGLClientBuffer)aScr->outBuf->getNativeBuffer();
    aScr->ost->eglImg = eglCreateImageKHR(mDpy, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, Clientbuf, 0);
    checkEglError("eglCreateImageKHR");
    if(EGL_NO_IMAGE_KHR == aScr->ost->eglImg) {
        ALOGE("[%s:%d] ERR: eglCreateImageKHR failed!", __FUNCTION__, __LINE__);
        return CREATE_IMG_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "[%s:%d] eglCreateImageKHR ok. egl img :%p", __FUNCTION__, __LINE__, (aScr->ost->eglImg));

    aScr->w = w;
    aScr->h = h;
    aScr->stride = w;
    aScr->size = w*h;
    aScr->format = HAL_PIXEL_FORMAT_BGRA_8888;

    ALOGI_IF(DEBUG_LOG, "createAdapterScreen end.");

    return NO_ERR;
}

int OffScreenObject::destroyAdapterScreen(adapter_screen_handle_s *aScr) {
    if(NULL == aScr) {
        return APT_SCR_ERR;
    }
    delete aScr->ost;
    aScr->ost = NULL;
    aScr->outBuf = NULL;
    ALOGI_IF(DEBUG_LOG, "OK : destroy Video Adapter handle.");
    return NO_ERR;
}

int OffScreenObject::createOffScreenTex(offscreen_texture_s *ost, GLuint texName) {
    if(NULL == ost) {
        return -1;
    }

    ALOGI_IF(DEBUG_LOG, "createOffScreenTex enter.");
    ost->outTexName = texName;
    ALOGD("[%s:%d] before gen texuture, outTex: %d, eglImg: %p" , __FUNCTION__, __LINE__ ,ost->outTex, ost->eglImg);
    glGenTextures(1, &(ost->outTex));
    checkEglError("glGenTextures");
    glBindTexture(GL_TEXTURE_2D, ost->outTex);
    checkEglError("glBindTexture");
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, (GLeglImageOES)ost->eglImg);
    checkEglError("glEGLImageTargetTexture2DOES");
    ALOGD("[%s:%d] after gen texuture, outTex: %d" , __FUNCTION__, __LINE__ ,ost->outTex);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    ALOGI_IF(DEBUG_LOG, "createOffScreenTex end.");
    return NO_ERR;
}

int OffScreenObject::destroyOffScreenTex(offscreen_texture_s *ost) {
    if(NULL == ost) {
        return -1;
    }
    ALOGE_IF(DEBUG_LOG, "destroyOffScreenTex enter.");
    glBindTexture(GL_TEXTURE_2D, 0);
    checkEglError("glBindTexture");

    glDeleteTextures(1, &(ost->outTex));
    bool ret = eglDestroyImageKHR(mDpy, ost->eglImg);
    if(ret){
        ALOGE("[%s:%d] ERR: eglDestroyImageKHR failed!", __FUNCTION__, __LINE__);
    }
    checkEglError("glDeleteTextures");
    ALOGE_IF(DEBUG_LOG, "destroyOffScreenTex end.");

    return NO_ERR;
}

int OffScreenObject::initScreen_ex(const int w, const int h) {

    int ret = NO_ERR;

    ALOGI_IF(DEBUG_LOG, "initScreen_ex enter.");
    setAdapterUsed(true);
    adapter_screen_handle_s *hAptScreen = NULL;
    //GPU output buffers
    adapter_screen_handle_s *AptScreen[NUMOUTPUTBUFFER] = {NULL, NULL};
    for(int i = 0; i < NUMOUTPUTBUFFER; i++) {
    if(isAdapterUsed()) {
        hAptScreen = new(adapter_screen_handle_s);
        if(NULL == hAptScreen)
        {
            return INIT_ERR;
        }
        if(EOK != memset_s(hAptScreen, sizeof(adapter_screen_handle_s), 0x00, sizeof(adapter_screen_handle_s))){
            ALOGE("memset_s failed !");
        }
        hAptScreen->ost = new(offscreen_texture_s);
        if(NULL == hAptScreen->ost)
        {
            delete hAptScreen;
            hAptScreen = NULL;
            return INIT_ERR;
        }
        if(EOK != memset_s(hAptScreen->ost, sizeof(offscreen_texture_s), 0x00, sizeof(offscreen_texture_s))){
            ALOGE("memset_s failed !");
        }

        ret = createAdapterScreen(hAptScreen, w, h);
        if(NO_ERR != ret) {
            ALOGE("[%s:%d] ERR: createAdapterBuffer", __FUNCTION__, __LINE__);

            delete hAptScreen->ost;
            hAptScreen->ost = NULL;

            delete hAptScreen;
            hAptScreen = NULL;

            return INIT_ERR;
        }
        ALOGD("createAdapterBuffer ok.");
         AptScreen[i] = hAptScreen;
        }
    }

    for(int i = 0; i < NUMOUTPUTBUFFER; i++) {
        hOffScreen[i].hVidScreen = new(video_screen_handle_s);
        if(EOK != memset_s(hOffScreen[i].hVidScreen, sizeof(video_screen_handle_s), 0x00, sizeof(video_screen_handle_s))){
            ALOGE("memset_s failed !");
        }
        ret = createVideoScreen(hOffScreen[i].hVidScreen, w, h, false);
        if(NO_ERR != ret) {
            ALOGE("[%s:%d] ERR: createVideoBuffer num [%d]", __FUNCTION__, __LINE__, i);
            return INIT_ERR;
        }
        hOffScreen[i].hVidScreen->num = i;
        ALOGD("createVideoBuffer num [%d] ok.", i);

        if(isAdapterUsed() || NULL != hAptScreen) {
            hOffScreen[i].hAptScreen = AptScreen[i];
            ALOGD("createAdapterBuffer num [%d] ok", i);
        }

        hOffScreen[i].hAptScreen->num = i;

        //The following is for new cell which video layer support ARGB.
        ret = createOffScreenTex(hOffScreen[i].hAptScreen->ost, GL_TEXTURE1+i);
        if(NO_ERR != ret) {
            ALOGE("[%s:%d] ERR: createOffScreenTex num [%d]", __FUNCTION__, __LINE__, i);
            return INIT_ERR;
        }
    }
    ALOGI_IF(DEBUG_LOG, "initScreen_ex end.");
    return NO_ERR;
}


int OffScreenObject::init(JNIEnv * env, jobject obj, const int w, const int h, const bool enableDebug, const bool enablePQ) {
    if(enableDebug){
         DEBUG_LOG = 1;
    }
    ALOGI_IF(DEBUG_LOG, "OutputBuffer init enter.");
    int ret = NO_ERR;

    createEGLContext(w, h);

    initScreen_ex(mWidth, mHeight);

    ret = mHal->voRM_Register();
    if (ret != NO_ERR) {
        ALOGE("voRM_Register failed, ret = 0x%x", ret);
    }


    ret = mHal->voRM_AcquireResource();
    if (ret != NO_ERR) {
        ALOGE("voRM_AcquireResource failed, ret = 0x%x", ret);
    }

    ret = mHal->voInit(mWidth, mHeight);
    if (ret != NO_ERR) {
        ALOGE("vo Init failed, ret = %d", ret);
        return ret;
    }
    ALOGI_IF(DEBUG_LOG, "hwGraphicssever vo_init");

    glViewport(0, 0, w, h);

    ALOGI_IF(DEBUG_LOG, "OffScreenObject init end.");
    mIsInitialized = true;
    return ret;
}

int OffScreenObject::deInit() {
    if (!mIsInitialized) {
        ALOGI("offscreenObject has not been initialized");
        return 0;
    }
    mIsInitialized = false;
    ALOGI_IF(DEBUG_LOG, "OffScreenObject deInit enter.");

    if(0 == mHal->voDeinit()){
        ALOGI_IF(DEBUG_LOG, "hwGraphicsLsever deInit");
    }

    if(0 == mHal->voRM_unregister()){
        ALOGI_IF(DEBUG_LOG, "hwGraphicsLsever voRM_unregister");
    }

    int ret = NO_ERR;

    if(NULL == hOffScreen[0].hVidScreen){
        ALOGW_IF(DEBUG_LOG, "OffScreenObject already deinit, no need again!");
        return ret;
    }
    for(int i = 0; i < NUMOUTPUTBUFFER; i++) {
        ret = destroyVideoScreen(hOffScreen[i].hVidScreen);
        if(NO_ERR != ret) {
            ALOGE("[%s:%d] ERR: OffScreenObject NUM[%d] destroyVideoScreen failed!", __FUNCTION__, __LINE__, i);
        } else {
            delete hOffScreen[i].hVidScreen;
            hOffScreen[i].hVidScreen = NULL;
        }

        ret = destroyOffScreenTex(hOffScreen[i].hAptScreen->ost);
        if(NO_ERR != ret) {
            ALOGE("[%s:%d] ERR: OffScreenObject NUM[%d] destroyOffScreenTex failed!", __FUNCTION__, __LINE__, i);
        } else {
            hOffScreen[i].hAptScreen->ost = NULL;
        }

        ret = destroyAdapterScreen(hOffScreen[i].hAptScreen);
        if(NO_ERR != ret) {
            ALOGE("[%s:%d] ERR: OffScreenObject NUM[%d] destroyAdapterScreen failed!", __FUNCTION__, __LINE__, i);
        } else {
            delete hOffScreen[i].hAptScreen;
            hOffScreen[i].hAptScreen = NULL;
        }
    }
    ALOGI_IF(DEBUG_LOG, "OffScreenObject destroy buffer ok");
    destroyEGLContext();

    ALOGI_IF(DEBUG_LOG, "OffScreenObject deInit end.");
    return ret;
}

int OffScreenObject:: getFormat(unsigned int *type)
{
    if (mHal == NULL) {
        mHal = IGraphics::getService();
    }
    if (mHal == NULL) {
        ALOGW("IGraphics getService failed!");
    }
    if(mHal != NULL) {
        *type = mHal->voGetFormat();
    }
    return HI_SUCCESS;
}

int OffScreenObject::swap(const int type) {
    if(mHal != NULL){
        mHal->voDoFramexSync();
    }

    glFinish();
    //eglSwapBuffers(mDpy, mSur);
    eglWaitClient();
    checkEglError("eglWaitClient");

    int outputIndex = bufCount+1;
    outputIndex %= NUMOUTPUTBUFFER;
    glBindTexture(GL_TEXTURE_2D, hOffScreen[outputIndex].hAptScreen->ost->outTex);
    checkEglError("glBindTexture");
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, (GLeglImageOES)hOffScreen[outputIndex].hAptScreen->ost->eglImg);
    checkEglError("glEGLImageTargetTexture2DOES");

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    unsigned char *buf = NULL;

    int ret = hOffScreen[bufCount].hAptScreen->outBuf->lock(GRALLOC_USAGE_SW_READ_OFTEN, (void**)(&buf));
    if(ret) {
        ALOGE("[%s:%d] ERR: AptBuffer lock failed!", __FUNCTION__, __LINE__);
        return SWAP_ERR;
    }

    int gpuphy = getGPUBufferPhy((private_handle_t*)hOffScreen[bufCount].hAptScreen->outBuf->handle);

    if(0== mHal->voDoFramex(hOffScreen[bufCount].hAptScreen->w, hOffScreen[bufCount].hAptScreen->h,  hOffScreen[bufCount].hAptScreen->stride, gpuphy))
    {
        ALOGI_IF(DEBUG_LOG, "hwGraphicssever voDoFramex");
    }
    char storageBitmap[PROPERTY_VALUE_MAX] = {0};
    property_get("persist.storageBitmap.support", storageBitmap, "false");
    if(strncmp(storageBitmap, "false", 5)){
        writeBitmap.w = hOffScreen[bufCount].hAptScreen->w;
        writeBitmap.h = hOffScreen[bufCount].hAptScreen->h;
        writeBitmap.mBuf = buf;
        pthread_t pThread;
        pthread_attr_t pProprety;
        pthread_attr_init(&pProprety);
        pthread_attr_setdetachstate(&pProprety,PTHREAD_CREATE_DETACHED);
        int err = pthread_create(&pThread,&pProprety,writeAfterBitmap_thread,&writeBitmap);
        if(err) {
             ALOGE("[%s:%d] ERR: pthread create failed", __FUNCTION__, __LINE__);
        }
        pthread_attr_destroy(&pProprety);
    }

    ret = hOffScreen[bufCount].hAptScreen->outBuf->unlock();
    if(ret) {
        ALOGE("[%s:%d] ERR: AptBuffer unlock failed!", __FUNCTION__, __LINE__);
        return SWAP_ERR;
    }

    ALOGI_IF(DEBUG_LOG, "swap bufCount %d", bufCount);
    bufCount++;
    bufCount %= NUMOUTPUTBUFFER;
    return NO_ERR;
}

GLuint OffScreenObject::getOffScreenTex() {
    GLuint tex = hOffScreen[bufCount].hAptScreen->ost->outTex;
    ALOGI_IF(DEBUG_LOG, "get tex bufCount: %d", bufCount);
    return tex;
}

GLuint OffScreenObject::getOffScreenTexName() {
    GLuint name = hOffScreen[bufCount].hAptScreen->ost->outTexName;
    ALOGI_IF(DEBUG_LOG, "get texname bufCount: %d name: %d", bufCount, name);
    return name;
}

#define LOG_TAG "OffScreenObject"

#include <securec.h>

#include <sys/mman.h>

#include <linux/ion.h>
#include <ion/ion.h>

#include "OffScreenObject.h"
//#include "VideoBuffer.h"

#include <hardware/gralloc.h>

//#include <tvos_hal_vout.h>
#include <utils/Log.h>

//test
#include <SkImageEncoder.h>
#include <SkBitmap.h>
#include <SkData.h>
#include <SkStream.h>
#include <cutils/properties.h>
#include <WorkerThread.h>
///
#include<sys/types.h>
#include<sys/stat.h>
#include<fcntl.h>
#include <vendor/hisilicon/hardware/hwgraphics/1.0/IGraphics.h>
#include <vendor/hisilicon/hardware/hwgraphics/1.0/types.h>

//using ::vendor::hisilicon::hardware::hwgalleryl::V1_0;
//::Status
using ::vendor::hisilicon::hardware::hwgraphics::V1_0::IGraphics;

#define HI_SUCCESS 0


int getGPUBufferPhy(struct private_handle_t* handle);
//#define GRALLOC_USAGE_PHYSICAL_MEM  (GRALLOC_USAGE_PRIVATE_3) //Alloc physical memory

#define WIDTH_K 1280
#define HEIGHT_K 720
#define WIDTH_2K 1920
#define HEIGHT_2K 1080
#define WIDTH_4K 4096
#define HEIGHT_4K 2160
#define WIDTH_S4K 3840
#define HEIGHT_S4K 2160

static int DEBUG_LOG =0;
static android::sp<IGraphics> mHal = NULL;

using namespace android;

static void checkEglError(const char* op, EGLBoolean returnVal = EGL_TRUE) {
    if (returnVal != EGL_TRUE) {
        ALOGE("%s() returned %d\n", op, returnVal);
    }

    for (EGLint error = eglGetError(); error != EGL_SUCCESS; error
            = eglGetError()) {
        ALOGE("after %s() (0x%x)\n", op, error);
    }
}


OffScreenObject::OffScreenObject()
    :bufCount(0) ,mWidth(0) ,mHeight(0)
    ,mDpy(NULL), mSur(NULL), mCtx(NULL),isAptUsed(false) {
    worker_thread_init();
    for(int i=0;i<NUMOUTPUTBUFFER;i++){
        hOffScreen[i].isAptUsed = false;
       // hOffScreen[i].hVidScreen = NULL;
        hOffScreen[i].hAptScreen = NULL;
    }
    if (mHal == NULL) {
        mHal = IGraphics::getService();
    }
    if (mHal == NULL) {
        ALOGW("IGraphics getService failed!");
    }
}

OffScreenObject::~OffScreenObject() {
}

int OffScreenObject::createEGLContext(int surfaceW, int surfaceH) {
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
        ALOGE("ERR: EGL No Display (%s)", __func__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglGetDisplay ok. ", __LINE__);

    int ret = eglInitialize(mDpy, &majorVersion, &minorVersion);
    if(EGL_TRUE != ret) {
        ALOGE("ERR: EGL Initialize (%s)", __func__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglInitialize ok. ", __LINE__);

    ret = eglChooseConfig(mDpy, dspConfigAttribs, &dspConfig, 1, &numConfig);

    EGLint pbufferAttribs[] = {
        EGL_WIDTH, surfaceW,
        EGL_HEIGHT, surfaceH,
        EGL_NONE
    };

    mSur = eglCreatePbufferSurface(mDpy, dspConfig, pbufferAttribs);
    if(EGL_NO_SURFACE == mSur) {
        ALOGE("ERR: EGL No Surface (%s)", __func__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglCreatePbufferSurface ok. ", __LINE__);

    EGLint contextAttribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
    mCtx = eglCreateContext(mDpy, dspConfig, EGL_NO_CONTEXT, contextAttribs);
    if(EGL_NO_CONTEXT == mCtx) {
        ALOGE("ERR: EGL No Context (%s)", __func__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglCreateContext ok. ", __LINE__);

    ret = eglMakeCurrent(mDpy, mSur, mSur, mCtx);
    if(EGL_TRUE != ret) {
        ALOGE("ERR: EGL eglMakeCurrent (%s)", __func__);
        return CREATE_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglMakeCurrent to PBuffer ok. ", __LINE__);

    eglQuerySurface(mDpy, mSur, EGL_WIDTH, &mWidth);
    eglQuerySurface(mDpy, mSur, EGL_HEIGHT, &mHeight);
    ALOGI_IF(DEBUG_LOG, "LINE: %d OutScreenObject after eglQuerySurface size[%d, %d] \n", mWidth, mHeight, __LINE__);

    return ret;
}

int OffScreenObject::destroyEGLContext() {
    int ret = EGL_TRUE;
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject destroyEGLContext . ", __LINE__);
    if(EGL_NO_DISPLAY == mDpy) {
        ALOGE("ERR: EGLDisplay already destroied (%s).", __func__);
        return DESTORY_CTX_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglMakeCurrent to NULL ok. ", __LINE__);
    ret = eglMakeCurrent(mDpy, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    if(EGL_TRUE != ret) {
        ALOGI("ERR: EGL eglMakeCurrent (%s)", __func__);
    }

    if(EGL_NO_SURFACE != mSur) {
        eglDestroySurface(mDpy, mSur);
        mSur = EGL_NO_SURFACE;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglMakeCurrent to NULL ok. ", __LINE__);
    if(EGL_NO_CONTEXT != mCtx) {
        eglDestroyContext(mDpy, mCtx);
        mCtx = EGL_NO_CONTEXT;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglDestroyContext ok. ", __LINE__);
    eglTerminate(mDpy);
    mDpy = EGL_NO_DISPLAY;
    return NO_ERR;
}


/*
int OffScreenObject::createVideoScreen(video_screen_handle_s *vScr, int w, int h, bool allocMem) {
    if(NULL != vScr->v_addr && 0 == vScr->phy_addr) {
        ALOGE("ERR: video screen handle has been created(%s)", __func__);
        return VID_SCR_ERR;
    }

    vScr->format = VOUT_FORMAT_YUV_SEMIPLANAR_420;

    if(!allocMem)
    {
        return NO_ERR;
    }

    int size = w*h*3/2;

    void * vaddr = NULL;
    uint64_t phyaddr = NULL;

    int ret;

    phyaddr = (uint64_t)HI_MMZ_New(size, 32, NULL, (HI_CHAR *)"HiGalleryl");
    if (NULL == phyaddr)
    {
        ALOGE("HI_MMZ_New HiGalleryl VideoScreen  failed!");
        HI_MMZ_Delete(phyaddr);
        return CREATE_SCREEN_ERR;
    }
    vaddr = HI_MMZ_Map(phyaddr, 1);

    vScr->v_addr =(unsigned char *) vaddr;
    vScr->phy_addr = phyaddr;
    vScr->w = w;
    vScr->h = h;
    vScr->stride = vScr->phy_size / h;
    vScr->size = size;
    vScr->format = VOUT_FORMAT_YUV_SEMIPLANAR_420;

    return NO_ERR;
}

int OffScreenObject::destroyVideoScreen(video_screen_handle_s *vScr) {
    if(NULL == vScr) {
        return VID_SCR_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "destroy video screen handle.LINE: %d ,vScr->phy_addr=(0x%x)" , __LINE__,vScr->phy_addr);
    HI_MMZ_Unmap(vScr->phy_addr);
    HI_MMZ_Delete(vScr->phy_addr);
    return NO_ERR;
}
*/
int OffScreenObject::createAdapterScreen(adapter_screen_handle_s *aScr, int w, int h) {
    if(NULL == aScr || NULL == aScr->ost){
        ALOGE("ERR: Adapter Screen handle has not been created.(%s)", __func__);
        return APT_SCR_ERR;
    }

    ALOGI_IF(DEBUG_LOG, "LINE %d enter createAdapterBuffer", __LINE__);

    aScr->outBuf = new GraphicBuffer(w, h, HAL_PIXEL_FORMAT_BGRA_8888, 1, GraphicBuffer::USAGE_HW_TEXTURE | GraphicBuffer::USAGE_HW_2D | GraphicBuffer::USAGE_SW_READ_OFTEN | GraphicBuffer::USAGE_SW_WRITE_OFTEN, "galleryl");
    if(NULL == aScr->outBuf->getNativeBuffer()){
        ALOGE("LINE %d alloc buffer error", __LINE__);
        return APT_SCR_ERR;
    }
    EGLClientBuffer Clientbuf = (EGLClientBuffer)aScr->outBuf->getNativeBuffer();
    aScr->ost->eglImg = eglCreateImageKHR(mDpy, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID, Clientbuf, 0);
    checkEglError("eglCreateImageKHR");
    if(EGL_NO_IMAGE_KHR == aScr->ost->eglImg) {
        ALOGE("ERR: EGL CREATE IMAGE ERR (%s)", __func__);
        return CREATE_IMG_ERR;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject eglCreateImageKHR ok. img :%p", __LINE__,(aScr->ost->eglImg));

    aScr->w = w;
    aScr->h = h;
    aScr->stride = 16384;
    aScr->size = w*h;
    aScr->format = HAL_PIXEL_FORMAT_BGRA_8888;

    ALOGI_IF(DEBUG_LOG, "LINE %d createAdapterScreen ok. ", __LINE__);

    return NO_ERR;
}

int OffScreenObject::destroyAdapterScreen(adapter_screen_handle_s *aScr) {
    if(NULL == aScr) {
        ALOGI_IF(DEBUG_LOG, "destroy destroyAdapterScreen handle return APT_SCR_ERR ,LINE %d ", __LINE__);
        return APT_SCR_ERR;
    }
    delete aScr;
    ALOGI_IF(DEBUG_LOG, "LINE %d destroy destroyAdapterScreen handle.", __LINE__);
    return NO_ERR;
}

int OffScreenObject::createOffScreenTex(offscreen_texture_s *ost, GLuint texName) {
    int ret = NO_ERR;
    if(NULL == ost) {
        return -1;
    }

    ALOGI_IF(DEBUG_LOG, "LINE %d enter createOffScreenTex ", __LINE__ );
    ost->outTexName = texName;
    ALOGI_IF(DEBUG_LOG, "LINE %d bf gen texuture %d img: %p" , __LINE__ ,ost->outTex, ost->eglImg);
    glGenTextures(1, &(ost->outTex));
    checkEglError("glGenTextures");
    glBindTexture(GL_TEXTURE_2D, ost->outTex);
    checkEglError("glBindTexture");
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, (GLeglImageOES)ost->eglImg);
    checkEglError("glEGLImageTargetTexture2DOES");
    ALOGI_IF(DEBUG_LOG, "LINE %d aft gen texuture %d" , __LINE__ ,ost->outTex);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    ALOGI_IF(DEBUG_LOG, "OK :createOffScreenTex ");
    return NO_ERR;
}

int OffScreenObject::destroyOffScreenTex(offscreen_texture_s *ost) {
    if(NULL == ost) {
        ALOGI_IF(DEBUG_LOG, "destroy offScreenTex return -1  LINE %d ", __LINE__);
        return -1;
    }
    ALOGI_IF(DEBUG_LOG, "destroy offScreenTex LINE %d ", __LINE__);
    glBindTexture(GL_TEXTURE_2D, 0);
    checkEglError("glBindTexture");

    glDeleteTextures(1, &(ost->outTex));
    eglDestroyImageKHR(mDpy, ost->eglImg);
    checkEglError("glDeleteTextures");
    ALOGI_IF(DEBUG_LOG, "OK : destroy offScreenTex ");

    return NO_ERR;
}

int OffScreenObject::initScreen_ex(int w, int h) {

    int ret = NO_ERR;

    ALOGI_IF(DEBUG_LOG, "LINE %d enter initScreen", __LINE__);
    setAdapterUsed(true);
    adapter_screen_handle_s *hAptScreen = NULL;
    //GPU output buffers
    adapter_screen_handle_s *AptScreen[NUMTEXTUREBUFFER] = {NULL};
    for(int i = 0; i < NUMTEXTUREBUFFER; i++) {
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
            ALOGE("ERR: createAdapterBuffer (%s)", __func__);
            delete hAptScreen->ost;
            hAptScreen->ost = NULL;

            delete hAptScreen;
            hAptScreen = NULL;
            return INIT_ERR;
        }
        ALOGI_IF(DEBUG_LOG, "OK: createAdapterBuffer %d",i);
        AptScreen[i] = hAptScreen;
        }
    }
	for(int j = 0;j < NUMOUTPUTBUFFER; j++)
    {
        hOffScreen[j].hAptScreen = AptScreen[0];
		hOffScreen[j].hAptScreen->num = j;
		int ret = createOffScreenTex(hOffScreen[j].hAptScreen->ost, GL_TEXTURE1+j);
        if(NO_ERR != ret) {
            ALOGE("ERR: createOffScreenTex num [%d] (%s)", j, __func__);
            return INIT_ERR;
         }
    }

/*
    for(int i = 0; i < NUMOUTPUTBUFFER; i++) {
        int j = 0;
        hOffScreen[i].hVidScreen = new(video_screen_handle_s);
        memset(hOffScreen[i].hVidScreen, 0x00, sizeof(video_screen_handle_s));

        ret = createVideoScreen(hOffScreen[i].hVidScreen, w, h, true);

        if(NO_ERR != ret) {
            ALOGE("ERR: createVideoBuffer num [%d] (%s)", i, __func__);
            return INIT_ERR;
        }
        hOffScreen[i].hVidScreen->num = i;
        ALOGI_IF(DEBUG_LOG, "OK: createVideoBuffer");

        if(isAdapterUsed() || NULL != hAptScreen) {
         if(i<NUMTEXTUREBUFFER){
                hOffScreen[i].hAptScreen = AptScreen[i];
             ALOGI_IF(DEBUG_LOG, "OK: createAdapterBuffer %d", i);
         }else{
                hOffScreen[i].hAptScreen = AptScreen[j];
             ALOGI_IF(DEBUG_LOG, "OK: createAdapterBuffer %d", j);
        }
        }

        hOffScreen[i].hAptScreen->num = i;

        //The following is for new cell which video layer support ARGB.
     if(i < NUMTEXTUREBUFFER){
            ret = createOffScreenTex(hOffScreen[i].hAptScreen->ost, GL_TEXTURE1+i);
         if(NO_ERR != ret) {
                ALOGE("ERR: createOffScreenTex num [%d] (%s)", i, __func__);
                return INIT_ERR;
            }
     }
    }
*/
    ALOGI_IF(DEBUG_LOG, "OK :init Screen");
    return NO_ERR;
}

int OffScreenObject::init(int w, int h, bool enableDebug, bool enablePQ) {
    if(enableDebug){
         DEBUG_LOG = 1;
    }

    ALOGI_IF(DEBUG_LOG, "OutputBuffer init enter.");
    int ret = NO_ERR;

    createEGLContext(w, h);

    initScreen_ex(mWidth, mHeight);

    if(0 == mHal->voInit(mWidth, mHeight)){
        ALOGI_IF(DEBUG_LOG, "hwGraphicssever vo_init");
    }

    if(0 == mHal->voWindowSetting(mWidth, mHeight)){
        ALOGI_IF(DEBUG_LOG, "hwGraphicssever vo_window_setting");
    }

    glViewport(0, 0, w, h);

    ALOGI_IF(DEBUG_LOG, "OutputBuffer init out.");
    return ret;
}

int OffScreenObject::deInit() {
    ALOGI_IF(DEBUG_LOG, "OutputBuffer deInit enter.");

    if(0 == mHal->voDeinit()){
        ALOGI_IF(DEBUG_LOG, "hwGraphicsLsever deInit");
    }

    int ret = NO_ERR;

    //if(NULL == hOffScreen[0].hVidScreen){
    //    ALOGI_IF(DEBUG_LOG, "OutputBuffer already deinit, no need again!");
    //    return ret;
    //}
    for(int i = 0; i < NUMOUTPUTBUFFER; i++) {
        /*
        ret = destroyVideoScreen(hOffScreen[i].hVidScreen);
        if(NO_ERR != ret) {
            ALOGE("ERR: destroyVideoScreen NUM[%d] destroyVideoScreen",i);
        } else {
            delete hOffScreen[i].hVidScreen;
            hOffScreen[i].hVidScreen = NULL;
            ALOGI_IF(DEBUG_LOG, "delete hOffScreen[%d].hVidScreen.LINE: %d ",i , __LINE__);
        }*/

        if(i < NUMTEXTUREBUFFER){
             ret = destroyOffScreenTex(hOffScreen[i].hAptScreen->ost);
             if(NO_ERR != ret) {
                 ALOGE("ERR: destroyOffScreenTex NUM[%d]destroyOffScreenTex",i);
          }else{
                 delete hOffScreen[i].hAptScreen->ost;
              hOffScreen[i].hAptScreen->ost = NULL;
          }

            ret = destroyAdapterScreen(hOffScreen[i].hAptScreen);
            if(NO_ERR != ret) {
                ALOGE("ERR: destroyAdapterScreen NUM[%d] destroyAdapterScreen",i);
            } else {
                hOffScreen[i].hAptScreen = NULL;
            }
        }

    }
    ALOGI_IF(DEBUG_LOG, "OK : OutScreenObject destroy");
    destroyEGLContext();

    ALOGI_IF(DEBUG_LOG, "OutputBuffer deInit out.");
    return ret;
}

int OffScreenObject:: getFormat(unsigned int *type)
{
    *type = mHal->voGetFormat();

    return HI_SUCCESS;
}

int OffScreenObject::swap(int type) {
    //vo_do_framex_sync();

    glFinish();
    eglWaitClient();
    checkEglError("eglWaitClient");

    int outputIndex = bufCount + 1;
    outputIndex %= NUMOUTPUTBUFFER;
    glBindTexture(GL_TEXTURE_2D, hOffScreen[outputIndex].hAptScreen->ost->outTex);
    checkEglError("glBindTexture");
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, (GLeglImageOES)hOffScreen[outputIndex].hAptScreen->ost->eglImg);
    checkEglError("glEGLImageTargetTexture2DOES");

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    unsigned char *buf = NULL;
    //size_t size = hOffScreen[bufCount].hVidScreen->phy_size;
    int ret = hOffScreen[bufCount].hAptScreen->outBuf->lock(GRALLOC_USAGE_SW_READ_OFTEN, (void**)(&buf));
    if(ret) {
        ALOGE("ERR: AptBuffer lock (%s)", __func__);
        return SWAP_ERR;
    }

    //vo_set_sync_attr(bufCount);

    int gpuphy = getGPUBufferPhy((private_handle_t*)(hOffScreen[bufCount].hAptScreen->outBuf->handle));

    //hOffScreen[bufCount].hVidScreen->format = VOUT_FORMAT_YUV_SEMIPLANAR_420;

    if(TYPE_NORMAL_k == type)
    {
        hOffScreen[bufCount].hAptScreen->w = WIDTH_K;
        hOffScreen[bufCount].hAptScreen->h = HEIGHT_K;
    }else if(TYPE_NORMAL_2k == type)
    {
        hOffScreen[bufCount].hAptScreen->w = WIDTH_2K;
        hOffScreen[bufCount].hAptScreen->h = HEIGHT_2K;
    }
     else if(TYPE_NORMAL_S4k == type)
    {
        hOffScreen[bufCount].hAptScreen->w = WIDTH_S4K;
        hOffScreen[bufCount].hAptScreen->h = HEIGHT_S4K;
    }else
    {
        hOffScreen[bufCount].hAptScreen->w = WIDTH_4K;
        hOffScreen[bufCount].hAptScreen->h = HEIGHT_4K;
    }
    if(0== mHal->voDoFramex(hOffScreen[bufCount].hAptScreen->w, hOffScreen[bufCount].hAptScreen->h,  hOffScreen[bufCount].hAptScreen->stride, gpuphy))
    {
        ALOGI_IF(DEBUG_LOG, "hwGraphicssever voDoFramex");
    }
    char saveGpuPic[PROPERTY_VALUE_MAX] = {0};
    property_get("vendor.higalleryl.savepic", saveGpuPic, "false");
    if (0 == strcmp(saveGpuPic, "true")) {
        int fd = -1;
        fd = open("/sdcard/gpuout.data", O_WRONLY | O_CREAT | O_TRUNC, 0664);
        if (fd == -1) {
            ALOGE("shawDebug ERR: create file bitmap.data failed. %d" , __LINE__);
        }

        write(fd, buf, hOffScreen[bufCount].hAptScreen->w * hOffScreen[bufCount].hAptScreen->h * 4);
        close(fd);
        ALOGI("hAptScreen[%d,%d]",hOffScreen[bufCount].hAptScreen->w,hOffScreen[bufCount].hAptScreen->h);
    }
/*
    ALOGI_IF(DEBUG_LOG, "vo_do_frame| Data:[w %d, h %d, stride %d, size %d, format %d, buf %p, phy_addr %x]",
            hOffScreen[bufCount].hAptScreen->w, hOffScreen[bufCount].hAptScreen->h,
            hOffScreen[bufCount].hAptScreen->stride , hOffScreen[bufCount].hAptScreen->size,
            (VOUT_VIDEO_FORMAT_E)hOffScreen[bufCount].hVidScreen->format,
            buf, (hOffScreen[bufCount].hVidScreen->phy_addr)
            );



    vo_do_framex(hOffScreen[bufCount].hAptScreen->w, hOffScreen[bufCount].hAptScreen->h,
            hOffScreen[bufCount].hAptScreen->stride , hOffScreen[bufCount].hAptScreen->size,
            (VOUT_VIDEO_FORMAT_E)hOffScreen[bufCount].hVidScreen->format,
            buf, gpuphy, (hOffScreen[bufCount].hVidScreen->v_addr),
            (hOffScreen[bufCount].hVidScreen->phy_addr),type);
*/
    ret = hOffScreen[bufCount].hAptScreen->outBuf->unlock();
    if(ret) {
        ALOGE("ERR: AptBuffer unlock (%s)", __func__);
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

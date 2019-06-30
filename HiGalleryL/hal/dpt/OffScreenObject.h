#ifndef _OUTPUTREDIRECT_H_
#define _OUTPUTREDIRECT_H_

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <ui/GraphicBuffer.h>
#include <android/native_window_jni.h>

#ifdef ION_V1
#include <ion/ion.h>
#endif
#define NUMOUTPUTBUFFER 3
#define CURRENT 0
#define TYPE_NORMAL_k 0
#define TYPE_NORMAL_2k 1
#define TYPE_NORMAL_S4k 2
#define TYPE_NORMAL_4k 3
#define TYPE_NORMAL_8k 4

namespace android {

//ERR output enum
enum {
    NO_ERR             = 0,
    CREATE_SCREEN_ERR  ,
    CREATE_IMG_ERR     ,
    DESTORY_CTX_ERR    ,
    CREATE_CTX_ERR     ,
    INIT_ERR           ,
    EGL_IMAGER_ERR     ,
    APT_SCR_ERR        ,
    VID_SCR_ERR        ,
    SWAP_ERR           ,
};

typedef struct {
        int w;
        int h;
        unsigned char *mBuf;
    }writeBitmap_handle_s;
writeBitmap_handle_s   writeBitmap;

class OffScreenObject {
private:
    typedef struct {
        GLuint          outTexName;
        GLuint          outTex;
        EGLImageKHR     eglImg;
    }offscreen_texture_s;

    typedef struct {

        int  num;
        int  w;
        int  h;
        int  stride;
        int  size;
        int  format;

        sp<GraphicBuffer>      outBuf;
        offscreen_texture_s    *ost;

    }adapter_screen_handle_s;

    typedef struct {

        int  num;
        int  w;
        int  h;
        int  format;
        int  stride;
        int  size;
        size_t phy_size;

        int  fd;
        int  mapfd;
        int  handle;

        unsigned char *v_addr;
        uint32_t phy_addr;
        offscreen_texture_s    *ost;

    }video_screen_handle_s;

    typedef struct {

        bool isAptUsed;
        video_screen_handle_s *hVidScreen;
        adapter_screen_handle_s *hAptScreen;

    }offscreen_handle_s;

public:


    OffScreenObject();
    ~OffScreenObject();

    //return EGL_TRUE for no err
    int createEGLContext(const int surfaceW, const int surfaceH);
    int destroyEGLContext();

    int init(JNIEnv * env, jobject obj, const int w, const int h, const bool enableDebug, const bool enablePQ);
    int deInit();

    int getOffScreenWidth() {return mWidth;}
    int getOffScreenHeight() {return mHeight;}

    int getErrData();
    //int getOutputScreenSize(int *w, int *h);

    inline bool isAdapterUsed() { return isAptUsed; }
    inline void setAdapterUsed(bool isUsed) { isAptUsed = isUsed; }

    GLuint getOffScreenTex();
    GLuint getOffScreenTexName();

    int sendToShow();

    int swap(const int type);
    int getFormat(unsigned int *type);

private:
    int initScreen_ex(const int w, const int h);

    int createVideoScreen(video_screen_handle_s *vScr, const int w, const int h, bool allocMem=true);
    int createAdapterScreen(adapter_screen_handle_s *aScr, const int w, const int h);
    int createOffScreenTex(offscreen_texture_s *ost, GLuint texName);

    int destroyVideoScreen(video_screen_handle_s *vScr);
    int destroyAdapterScreen(adapter_screen_handle_s *aScr);
    int destroyOffScreenTex(offscreen_texture_s *ost);


    offscreen_handle_s     hOffScreen[NUMOUTPUTBUFFER];


//public:
    int bufCount;
    int mWidth;
    int mHeight;

    EGLDisplay             mDpy;
    EGLSurface             mSur;
    EGLContext             mCtx;

    bool isAptUsed;
    bool mIsInitialized;

}__attribute__ ((aligned (64)));
};

#endif

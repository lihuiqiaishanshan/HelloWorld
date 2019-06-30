#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <utils/Log.h>

#include "OffScreenObject.h"
using namespace android;
#define TLOGE(...) ALOGE("LINE: %d ", __LINE__, __VA_ARGS__)

int test_1(OffScreenObject *);

int main(int argc, char **argv) {

    int ret = NO_ERR;
    OffScreenObject *obj;

    obj = new OffScreenObject();

    ret = test_1(obj);


    return ret;
}

int test_1(OffScreenObject *obj) {
    int ret = obj->init();
    if(NO_ERR != ret) {
        TLOGE("ERR");
        return ret;
    }
    TLOGE("init ok");
    ret = obj->deInit();
    if(NO_ERR != ret) {
        TLOGE("ERR");
        return ret;
    }
    TLOGE("de init ok");
    TLOGE("test ok");
    return NO_ERR;
}

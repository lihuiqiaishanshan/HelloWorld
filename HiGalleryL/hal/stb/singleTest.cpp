
#include <stdlib.h>
#include <stdio.h>
#include <time.h>
#include <sched.h>
#include <sys/resource.h>

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <utils/Timers.h>
#include <utils/Log.h>

#include <securec.h>

#include "OffScreenObject.h"

using namespace android;
#define DEBUG_LOG 1

static const char gVertexShader[] = "attribute vec4 vPosition;\n "
    "void main (void){\n"
    "gl_Position = vPosition;\n"
    "}\n";

static const char gFragmentShader[] = "precision mediump float;\n"
    "void main() {\n"
    "  gl_FragColor = vec4(0.3, 0.1, 0.7, 0.5);\n"
    "}\n";

static const char gSimpleVS[] =
    "attribute vec4 postion;\n"
    "void main (void) {\n"
    "gl_Position = postion;\n"
    "}\n";

static const char gSimpleFS[] =
    "precision mediump float;\n"
    "void main (void) {\n"
    " gl_FragColor = vec4(1.0, 1.0, 0.0, 0.5);\n"
    "}\n";


GLuint loadShader(GLenum shaderType, const char *pSource){
    GLuint shader = glCreateShader(shaderType);
    if(shader) {
        glShaderSource(shader , 1, &pSource, NULL);
        glCompileShader(shader);

        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char* buf = (char*) malloc(infoLen);
                if(EOK != memset_s(buf, infoLen, 0x00, infoLen)){
                    ALOGE("memset_s failed !");
                }
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    fprintf(stderr, "Could not compile shader %d:\n%s\n",
                            shaderType, buf);
                    free(buf);
                    buf = NULL;
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

GLuint createProgram(const char* pVertexSource, const char *pFragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if(!vertexShader) {
        ALOGE("%d ERR: vertexShader load", __LINE__);
        return 0;
    }
    GLuint fragmentShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if(!fragmentShader) {
        ALOGE("%d ERR: fragmentShader load", __LINE__);
        return 0;
    }


    GLuint program = glCreateProgram();
    if(program) {
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char* buf = (char*) malloc(bufLength);
                if (EOK != memset_s(buf, bufLength, 0x00, bufLength)){
                    ALOGE("memset_s failed !");
                }
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    fprintf(stderr, "Could not link program:\n%s\n", buf);
                    free(buf);
                    buf = NULL;
                }
            }
            glDeleteProgram(program);
            program = 0;
        }

    }
    return program;
}

GLuint gProgram;
GLuint gFboProgram;
GLuint gvPositionHandle;
GLuint gvPosFBHandle;

GLuint gFbo;
GLuint gTexture;

static OffScreenObject *obj = NULL;

const GLfloat gVertexFbo[] = {
    -1.0f, -1.0f,
     1.0f, -1.0f,
     0.0f,  1.0f,
};
const GLfloat gVertexScreen[] = {
    -1.0f, -1.0f, 0,
     1.0f, -1.0f, 0,
    -1.0f,  1.0f, 0,
     1.0f,  1.0f, 0,
};

int setupGraphic(int w, int h){
    ALOGE("%s [%d %d]", __func__ ,w, h);
    gProgram = createProgram(gVertexShader, gFragmentShader);
    if(!gProgram) {
        ALOGE("%s ERR: gProgram " , __func__);
        return -1;
    }
    gvPositionHandle = glGetAttribLocation(gProgram, "vPostion");

    gFboProgram = createProgram(gSimpleVS, gSimpleFS);
    if(!gFboProgram) {
        ALOGE("%s ERR: gFboProgram " , __func__);
        return -1;
    }

    gvPosFBHandle = glGetAttribLocation(gFboProgram, "postion");


    int ret = obj->createOffScreenTex();
    if(ret != NO_ERR) {
        ALOGE("%s ERR createOffScreenTex ", __func__);
        return -1;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject createOffScreenTex ok", __LINE__);

    gTexture = obj->mTex[0];

    glGenFramebuffers(1, &gFbo);
    glBindFramebuffer(GL_FRAMEBUFFER, gFbo);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gTexture, 0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glViewport(0 ,0, w, h);
    return 0;
}

void renderFrame(int w, int h) {
    //ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject renderFrame", __LINE__);

    glBindFramebuffer(GL_FRAMEBUFFER, gFbo);
    glViewport(0, 0, w, h);

    glClearColor(0.0f,0.0f,0.0f,0.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glUseProgram(gProgram);

    glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gVertexFbo);
    glEnableVertexAttribArray(gvPositionHandle);
    glDrawArrays(GL_TRIANGLES, 0, 3);

    glViewport(w*0.1, h*0.1, w/2, h/2);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    glUseProgram(gFboProgram);
    glEnable(GL_BLEND);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

    glVertexAttribPointer(gvPosFBHandle, 3, GL_FLOAT, GL_FALSE, 0, gVertexScreen);
    glEnableVertexAttribArray(gvPosFBHandle);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
//    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject renderFrame ok", __LINE__);

}

int main() {
    obj = new OffScreenObject();
    int ret = obj->init();
    if(NO_ERR != ret) {
        return -1;
    }

    int w = obj->mWidth;
    int h = obj->mHeight;

    ret = setupGraphic(w, h);
    if(ret)
    {
        ALOGE("ERR: LINE %d OutScreenObject setupGraphic", __LINE__);
        return -1;
    }
    ALOGI_IF(DEBUG_LOG, "LINE %d OutScreenObject setupGraphic ok", __LINE__);

    while(1) {
        renderFrame(w ,h);
        obj->swap();
    }
    return 0;
}




#ifndef ESUTIL_H
#define ESUTIL_H

#include <stdlib.h>

#include <EGL/egl.h>
#include <EGL/eglplatform.h>
#include <GLES2/gl2.h>
#include <jni.h>
#include <sys/cdefs.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <string.h>

__BEGIN_DECLS

#define PI 3.1415926535897932384626433832795f

#define ROTATION_0      0
#define ROTATION_90     90
#define ROTATION_270    270

#define PICTURE_1080P    1920 * 1080

#define bool int
#define true 1
#define false 0


typedef struct
{
   GLfloat   m[4][4];
} ESMatrix;

//view mode
enum {
    ORIGINAL_MODE             = 0,
    FULLSCREEN_MODE,
    AUTO_MODE,
    SCALE_MODE,
};

GLfloat *vVertices;
GLuint gScreenWidth;
GLuint gScreenHeight;
int gRotationDegree;
bool gFullScreen;
int gViewMode;

float gTotalX;
float gTotalY;
float gTransMaxX;
float gTransMaxY;
float gTotalScaleX;
float gTotalScaleY;

#define  LOG_TAG    "gallerycore"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define DEBUG

///
//  Public Functions
//

//
///
/// \brief Load a shader, check for compile errors, print error messages to output log
/// \param type Type of shader (GL_VERTEX_SHADER or GL_FRAGMENT_SHADER)
/// \param shaderSrc Shader source string
/// \return A new shader object on success, 0 on failure
//
GLuint esLoadShader ( GLenum shaderType, const char *pSource );

//
///
/// \brief Load a vertex and fragment shader, create a program object, link program.
///        Errors output to log.
/// \param vertShaderSrc Vertex shader source code
/// \param fragShaderSrc Fragment shader source code
/// \return A new program object linked with the vertex/fragment shader pair, 0 on failure
//
GLuint esLoadProgram ( const char *pVertexSource, const char *pFragmentSource );


//
/// \brief multiply matrix specified by result with a scaling matrix and return new matrix in result
/// \param result Specifies the input matrix.  Scaled matrix is returned in result.
/// \param sx, sy, sz Scale factors along the x, y and z axes respectively
//
void esScale ( ESMatrix *result, GLfloat sx, GLfloat sy, GLfloat sz );

//
/// \brief multiply matrix specified by result with a translation matrix and return new matrix in result
/// \param result Specifies the input matrix.  Translated matrix is returned in result.
/// \param tx, ty, tz Scale factors along the x, y and z axes respectively
//
void esTranslate ( ESMatrix *result, GLfloat tx, GLfloat ty, GLfloat tz );

//
/// \brief multiply matrix specified by result with a rotation matrix and return new matrix in result
/// \param result Specifies the input matrix.  Rotated matrix is returned in result.
/// \param angle Specifies the angle of rotation, in degrees.
/// \param x, y, z Specify the x, y and z coordinates of a vector, respectively
//
void esRotate ( ESMatrix *result, GLfloat angle, GLfloat x, GLfloat y, GLfloat z );

//
/// \brief perform the following operation - result matrix = srcA matrix * srcB matrix
/// \param result Returns multiplied matrix
/// \param srcA, srcB Input matrices to be multiplied
//
void esMatrixMultiply ( ESMatrix *result, ESMatrix *srcA, ESMatrix *srcB );

//
//// \brief return an indentity matrix
//// \param result returns identity matrix
//
void esMatrixLoadIdentity ( ESMatrix *result );

//
//// \brief calculate coordinate
//// \param rotation scale rotation
//// \param picWidth picture's width
//// \param picHeight picture's height
//
void esCalcCoordinate(int rotation, GLuint picWidth, GLuint picHeight);

//
/// \for hisi
/// \brief multiply matrix specified by result with a scaling matrix and return new matrix in result
/// \param result Specifies the input matrix.  Scaled matrix is returned in result.
/// \param sx, sy, sz Scale factors along the x, y and z axes respectively
//
void hisiScale ( ESMatrix *result, GLfloat sx, GLfloat sy, GLfloat sz );

//
/// \for hisi
/// \brief multiply matrix specified by result with a translation matrix and return new matrix in result
/// \param result Specifies the input matrix.  Translated matrix is returned in result.
/// \param tx, ty, tz Scale factors along the x, y and z axes respectively
//
bool hisiTranslate ( ESMatrix *result, GLfloat tx, GLfloat ty, GLfloat tz );

__END_DECLS

#endif // ESUTIL_H


#include "esUtil.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stddef.h>

#include <securec.h>


//
///
/// \brief Load a shader, check for compile errors, print error messages to output log
/// \param shaderType Type of shader (GL_VERTEX_SHADER or GL_FRAGMENT_SHADER)
/// \param pSource Shader source string
/// \return A new shader object on success, 0 on failure
//
GLuint esLoadShader ( GLenum shaderType, const char *pSource )
{
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char* buf = (char*) malloc(infoLen);
                if (EOK != memset_s(buf, infoLen, 0x00, infoLen)){
                    LOGE("memset_s failed !");
                }
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile shader %d:\n%s\n",
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


//
///
/// \brief Load a vertex and fragment shader, create a program object, link program.
//         Errors output to log.
/// \param pVertexSource Vertex shader source code
/// \param pFragmentSource Fragment shader source code
/// \return A new program object linked with the vertex/fragment shader pair, 0 on failure
//
GLuint esLoadProgram ( const char* pVertexSource, const char* pFragmentSource )
{
   GLuint vertexShader;
   GLuint fragmentShader;
   GLuint programObject;
   GLint linked = 0;

   // Load the vertex/fragment shaders
   vertexShader = esLoadShader ( GL_VERTEX_SHADER, pVertexSource );

   if ( vertexShader == 0 )
   {
      return 0;
   }

   fragmentShader = esLoadShader ( GL_FRAGMENT_SHADER, pFragmentSource );

   if ( fragmentShader == 0 )
   {
      glDeleteShader ( vertexShader );
      return 0;
   }

   // Create the program object
   programObject = glCreateProgram ( );

   if ( programObject == 0 )
   {
      return 0;
   }

   glAttachShader ( programObject, vertexShader );
   glAttachShader ( programObject, fragmentShader );

   // Link the program
   glLinkProgram ( programObject );

   // Check the link status
   glGetProgramiv ( programObject, GL_LINK_STATUS, &linked );

   if ( !linked )
   {
      GLint infoLen = 0;

      glGetProgramiv ( programObject, GL_INFO_LOG_LENGTH, &infoLen );

      if ( infoLen > 1 )
      {
         char *infoLog = malloc ( sizeof ( char ) * infoLen );
         if (EOK != memset_s(infoLog, sizeof ( char ) * infoLen, 0x00, sizeof ( char ) * infoLen)){
             LOGE("memset_s failed !");
         }

         glGetProgramInfoLog ( programObject, infoLen, NULL, infoLog );
         LOGE("Could not link program:\n%s\n", infoLog);

         free ( infoLog );
         infoLog = NULL;
      }

      glDeleteProgram ( programObject );
      //programObject = 0;
      return 0;
   }

   // Free up no longer needed shader resources
   /*glDeleteShader ( *pVertexSource );
   glDeleteShader ( *pFragmentSource );*/
   glDeleteShader(vertexShader);
   glDeleteShader(fragmentShader);

   return programObject;
}

void esMatrixLoadIdentity(ESMatrix *result) {
    if (EOK != memset_s(result, sizeof(ESMatrix), 0x00, sizeof(ESMatrix))){
        LOGE("memset_s failed !");
    }
    result->m[0][0] = 1.0f;
    result->m[1][1] = 1.0f;
    result->m[2][2] = 1.0f;
    result->m[3][3] = 1.0f;
}

void esMatrixMultiply(ESMatrix *result, ESMatrix *srcA, ESMatrix *srcB) {
    ESMatrix tmp;
    int i;

    for (i = 0; i < 4; i++) {
        tmp.m[i][0] = (srcA->m[i][0] * srcB->m[0][0])
                    + (srcA->m[i][1] * srcB->m[1][0])
                    + (srcA->m[i][2] * srcB->m[2][0])
                    + (srcA->m[i][3] * srcB->m[3][0]);

        tmp.m[i][1] = (srcA->m[i][0] * srcB->m[0][1])
                    + (srcA->m[i][1] * srcB->m[1][1])
                    + (srcA->m[i][2] * srcB->m[2][1])
                    + (srcA->m[i][3] * srcB->m[3][1]);

        tmp.m[i][2] = (srcA->m[i][0] * srcB->m[0][2])
                    + (srcA->m[i][1] * srcB->m[1][2])
                    + (srcA->m[i][2] * srcB->m[2][2])
                    + (srcA->m[i][3] * srcB->m[3][2]);

        tmp.m[i][3] = (srcA->m[i][0] * srcB->m[0][3])
                    + (srcA->m[i][1] * srcB->m[1][3])
                    + (srcA->m[i][2] * srcB->m[2][3])
                    + (srcA->m[i][3] * srcB->m[3][3]);
    }

    if (EOK != memcpy_s(result, sizeof(ESMatrix), &tmp, sizeof(ESMatrix))){
        LOGE("memcpy_s failed !");
    }
}

void esTranslate(ESMatrix *result, GLfloat tx, GLfloat ty, GLfloat tz) {
    result->m[3][0] += (result->m[0][0] * tx + result->m[1][0] * ty
            + result->m[2][0] * tz);
    result->m[3][1] += (result->m[0][1] * tx + result->m[1][1] * ty
            + result->m[2][1] * tz);
    result->m[3][2] += (result->m[0][2] * tx + result->m[1][2] * ty
            + result->m[2][2] * tz);
    result->m[3][3] += (result->m[0][3] * tx + result->m[1][3] * ty
            + result->m[2][3] * tz);
}

bool hisiTranslate(ESMatrix *result, GLfloat tx, GLfloat ty, GLfloat tz) {
    const int BASE_LEVEL = 1000000;
    int total = 0;
    int transMax = 0;
    int stride = 0;
    float gTotalX_r = gTotalX;
    float gTotalY_r = gTotalY;
    gTotalX += tx;
    gTotalY += ty;

    total = (int)(gTotalX * BASE_LEVEL);
    transMax = (int)(gTransMaxX * BASE_LEVEL);
    stride = (int)(tx * BASE_LEVEL);
    if(fabs(total) > transMax){
        if(fabs(fabs(total) - transMax) <= fabs(stride)){
            if(tx < 0.0f){
                tx = -1.0f * fabs(gTransMaxX - fabs(gTotalX - tx));
                if(tx == 0 && gTotalX_r > 0)
                    tx = -1.0f * gTransMaxX;
            }
            else{
                tx = fabs(gTransMaxX - fabs(gTotalX - tx));
                if(tx == 0 && gTotalX_r < 0)
                    tx = gTransMaxX;
            }

            gTotalX = gTotalX_r + tx;
        }
        else{
            gTotalX -= tx;
            return false;
        }
    }

    total = (int)(gTotalY * BASE_LEVEL);
    transMax = (int)(gTransMaxY * BASE_LEVEL);
    stride = (int)(ty * BASE_LEVEL);
    if(fabs(total) > transMax){
        if(fabs(fabs(total) - transMax) <= fabs(stride)){
            if(ty < 0.0f){
                ty = -1.0f * fabs(gTransMaxY - fabs(gTotalY - ty));
                if(ty == 0 && gTotalY_r > 0)
                    ty = -1.0f * gTransMaxY;
            }
            else{
                ty = fabs(gTransMaxY - fabs(gTotalY - ty));
                if(ty == 0 && gTotalY_r < 0)
                    ty = gTransMaxY;
            }

            gTotalY = gTotalY_r + ty;
        }
        else{
            gTotalY -= ty;
            return false;
        }
    }

    esTranslate(result, tx, ty, tz);
    return true;
}

void esScale(ESMatrix *result, GLfloat sx, GLfloat sy, GLfloat sz) {
    result->m[0][0] *= sx;
    result->m[0][1] *= sx;
    result->m[0][2] *= sx;
    result->m[0][3] *= sx;

    result->m[1][0] *= sy;
    result->m[1][1] *= sy;
    result->m[1][2] *= sy;
    result->m[1][3] *= sy;

    result->m[2][0] *= sz;
    result->m[2][1] *= sz;
    result->m[2][2] *= sz;
    result->m[2][3] *= sz;
}

void hisiScale(ESMatrix *result, GLfloat sx, GLfloat sy, GLfloat sz) {
    float totalX = gTotalX;
    float totalY = gTotalY;
    if(fabs(gTotalX) > 0.0f || fabs(gTotalY) > 0.0f){
        hisiTranslate(result, -1.0f * gTotalX, -1.0f * gTotalY, 0.0f);
        gTotalX = 0.0f;
        gTotalY = 0.0f;
    }
    gTotalScaleX *= sx;
    gTotalScaleY *= sy;
    gTransMaxX = fabs((vVertices[15] * gTotalScaleX - 1.0f) / gTotalScaleX);
    gTransMaxY = fabs((vVertices[16] * gTotalScaleY - 1.0f) / gTotalScaleY);

    esScale(result, sx, sy, sz);

    if(fabs(totalX) > gTransMaxX){
        if(totalX < 0.0f){
            totalX = -1.0f * gTransMaxX;
        }
        else{
            totalX = gTransMaxX;
        }
    }

    if(fabs(totalY) > gTransMaxY){
        if(totalY < 0.0f){
            totalY = -1.0f * gTransMaxY;
        }
        else{
            totalY = gTransMaxY;
        }
    }
    hisiTranslate(result, totalX, totalY, 0.0f);
}

void esRotate(ESMatrix *result, GLfloat angle, GLfloat x, GLfloat y, GLfloat z) {
    GLfloat sinAngle, cosAngle;
    GLfloat mag = sqrtf(x * x + y * y + z * z);

    sinAngle = sinf(angle * PI / 180.0f);
    cosAngle = cosf(angle * PI / 180.0f);

    if (mag > 0.0f) {
        GLfloat xx, yy, zz, xy, yz, zx, xs, ys, zs;
        GLfloat oneMinusCos;
        ESMatrix rotMat;

        x /= mag;
        y /= mag;
        z /= mag;

        xx = x * x;
        yy = y * y;
        zz = z * z;
        xy = x * y;
        yz = y * z;
        zx = z * x;
        xs = x * sinAngle;
        ys = y * sinAngle;
        zs = z * sinAngle;
        oneMinusCos = 1.0f - cosAngle;

        rotMat.m[0][0] = (oneMinusCos * xx) + cosAngle;
        rotMat.m[0][1] = (oneMinusCos * xy) - zs;
        rotMat.m[0][2] = (oneMinusCos * zx) + ys;
        rotMat.m[0][3] = 0.0F;

        rotMat.m[1][0] = (oneMinusCos * xy) + zs;
        rotMat.m[1][1] = (oneMinusCos * yy) + cosAngle;
        rotMat.m[1][2] = (oneMinusCos * yz) - xs;
        rotMat.m[1][3] = 0.0F;

        rotMat.m[2][0] = (oneMinusCos * zx) - ys;
        rotMat.m[2][1] = (oneMinusCos * yz) + xs;
        rotMat.m[2][2] = (oneMinusCos * zz) + cosAngle;
        rotMat.m[2][3] = 0.0F;

        rotMat.m[3][0] = 0.0F;
        rotMat.m[3][1] = 0.0F;
        rotMat.m[3][2] = 0.0F;
        rotMat.m[3][3] = 1.0F;

        esMatrixMultiply(result, &rotMat, result);
    }
}

void esCalcCoordinate(int degree, GLuint picWidth, GLuint picHeight){
    float picAspect;
    float screenAspect;
    GLfloat vTmp[] = {
            -1, 1, 0.0f,        // Position 0
            //0.0f, 0.0f,         // TexCoord 0
            0.0f, 1.0f,

            -1, -1, 0.0f,       // Position 1
            //0.0f, 1.0f,         // TexCoord 1
            0.0f, 0.0f,

            1, -1, 0.0f,        // Position 2
            //1.0f, 1.0f,         // TexCoord 2
            1.0f, 0.0f,

            1, 1, 0.0f,         // Position 3
           //1.0f, 0.0f         // TexCoord 3
            1.0f, 1.0f
    };

    if (EOK != memset_s(vVertices, sizeof(vTmp), 0x00, sizeof(vTmp))){
        LOGE("memset_s failed !");
    }
    if (EOK != memcpy_s(vVertices, sizeof(vTmp), vTmp, sizeof(vTmp))){
        LOGE("memcpy_s failed !");
    }
    if(gViewMode == SCALE_MODE){
        return;
    }

    if((ROTATION_90 == degree || ROTATION_270 == degree) && ROTATION_0 == gRotationDegree){
        gRotationDegree = ROTATION_90;
    }
    else{
        gRotationDegree = ROTATION_0;
    }

    // Compute the window aspect ratio
    switch(gRotationDegree){
    case ROTATION_0:
        picAspect = (GLfloat) picWidth / (GLfloat) picHeight;
        screenAspect = (GLfloat) gScreenWidth / (GLfloat) gScreenHeight;
        if(gFullScreen){ // full green
            if(picWidth > picHeight){
                if(picAspect > screenAspect){
                    vVertices[1] *= screenAspect / picAspect;
                    vVertices[6] *= screenAspect / picAspect;
                    vVertices[11] *= screenAspect / picAspect;
                    vVertices[16] *= screenAspect / picAspect;
                }
                else if(picAspect < screenAspect){
                    vVertices[0] *= picAspect / screenAspect;
                    vVertices[5] *= picAspect / screenAspect;
                    vVertices[10] *= picAspect / screenAspect;
                    vVertices[15] *= picAspect / screenAspect;
                }
            }
            else if(picWidth <= picHeight){
                vVertices[0] *= picAspect / screenAspect;
                vVertices[5] *= picAspect / screenAspect;
                vVertices[10] *= picAspect / screenAspect;
                vVertices[15] *= picAspect / screenAspect;
            }
        }
        else { // original size
            if(picWidth >= gScreenWidth || picHeight >= gScreenHeight){
                if(picWidth > picHeight){
                    if(picAspect > screenAspect){
                        vVertices[1] *= screenAspect / picAspect;
                        vVertices[6] *= screenAspect / picAspect;
                        vVertices[11] *= screenAspect / picAspect;
                        vVertices[16] *= screenAspect / picAspect;
                    }
                    else if(picAspect < screenAspect){
                        vVertices[0] *= picAspect / screenAspect;
                        vVertices[5] *= picAspect / screenAspect;
                        vVertices[10] *= picAspect / screenAspect;
                        vVertices[15] *= picAspect / screenAspect;
                    }
                }
                else if(picWidth <= picHeight){
                    vVertices[0] *= picAspect / screenAspect;
                    vVertices[5] *= picAspect / screenAspect;
                    vVertices[10] *= picAspect / screenAspect;
                    vVertices[15] *= picAspect / screenAspect;
                }
            }
            else{
                vVertices[0] *= (GLfloat)picWidth / (GLfloat)gScreenWidth;
                vVertices[5] *= (GLfloat)picWidth / (GLfloat)gScreenWidth;
                vVertices[10] *= (GLfloat)picWidth / (GLfloat)gScreenWidth;
                vVertices[15] *= (GLfloat)picWidth / (GLfloat)gScreenWidth;

                vVertices[1] *= (GLfloat)picHeight / (GLfloat)gScreenHeight;
                vVertices[6] *= (GLfloat)picHeight / (GLfloat)gScreenHeight;
                vVertices[11] *= (GLfloat)picHeight / (GLfloat)gScreenHeight;
                vVertices[16] *= (GLfloat)picHeight / (GLfloat)gScreenHeight;
            }
        }
        break;
    case ROTATION_90:
        picAspect = (GLfloat) picHeight / (GLfloat) picWidth;
        screenAspect = (GLfloat) gScreenWidth / (GLfloat) gScreenHeight;
        if(gFullScreen){ // full green
            if(picHeight > picWidth){
                if(picAspect > screenAspect){
                    vVertices[0] *= screenAspect / picAspect;
                    vVertices[5] *= screenAspect / picAspect;
                    vVertices[10] *= screenAspect / picAspect;
                    vVertices[15] *= screenAspect / picAspect;
                }
                else if(picAspect < screenAspect){
                    vVertices[1] *= picAspect / screenAspect;
                    vVertices[6] *= picAspect / screenAspect;
                    vVertices[11] *= picAspect / screenAspect;
                    vVertices[16] *= picAspect / screenAspect;
                }
            }
            else if(picHeight <= picWidth){
                vVertices[1] *= picAspect / screenAspect;
                vVertices[6] *= picAspect / screenAspect;
                vVertices[11] *= picAspect / screenAspect;
                vVertices[16] *= picAspect / screenAspect;
            }
        }
        else { // original size
            if(picWidth >= gScreenWidth || picHeight >= gScreenHeight){
                if(picHeight > picWidth){
                    if(picAspect > screenAspect){
                        vVertices[0] *= screenAspect / picAspect;
                        vVertices[5] *= screenAspect / picAspect;
                        vVertices[10] *= screenAspect / picAspect;
                        vVertices[15] *= screenAspect / picAspect;
                    }
                    else if(picAspect < screenAspect){
                        vVertices[1] *= picAspect / screenAspect;
                        vVertices[6] *= picAspect / screenAspect;
                        vVertices[11] *= picAspect / screenAspect;
                        vVertices[16] *= picAspect / screenAspect;
                    }
                }
                else if(picHeight <= picWidth){
                    vVertices[1] *= picAspect / screenAspect;
                    vVertices[6] *= picAspect / screenAspect;
                    vVertices[11] *= picAspect / screenAspect;
                    vVertices[16] *= picAspect / screenAspect;
                }
            }
            else{
                vVertices[0] *= (GLfloat)picWidth / (GLfloat)gScreenHeight;
                vVertices[5] *= (GLfloat)picWidth / (GLfloat)gScreenHeight;
                vVertices[10] *= (GLfloat)picWidth / (GLfloat)gScreenHeight;
                vVertices[15] *= (GLfloat)picWidth / (GLfloat)gScreenHeight;

                vVertices[1] *= (GLfloat)picHeight / (GLfloat)gScreenWidth;
                vVertices[6] *= (GLfloat)picHeight / (GLfloat)gScreenWidth;
                vVertices[11] *= (GLfloat)picHeight / (GLfloat)gScreenWidth;
                vVertices[16] *= (GLfloat)picHeight / (GLfloat)gScreenWidth;
            }
        }
        break;
    default:
        break;
    }
}


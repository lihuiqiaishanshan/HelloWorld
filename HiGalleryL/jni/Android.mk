# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#LOCAL_32_BIT_ONLY := true

LOCAL_MULTILIB := 32
LOCAL_MODULE    := libgallerycore
#LOCAL_CFLAGS    := -Werror
#LOCAL_LDFLAGS   := -lm -llog -landroid -lGLESv2 -lEGL -ljnigraphics
LOCAL_LDLIBS    := -lm -llog -landroid -lGLESv2 -lEGL -ljnigraphics

#LOCAL_STATIC_LIBRARIES := android_native_app_glue
LOCAL_STATIC_LIBRARIES := libskia libwebp-decode libwebp-encode
LOCAL_SHARED_LIBRARIES += libjpeg liboffscreenobject libutils libcutils libjnigraphics libandroid

#before lollipop
ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) < 21 ))" )))
LOCAL_SHARED_LIBRARIES += libEGL \
                          libGLESv1_CM \
                          libGLESv2 \
                          libjnigraphics \
                          libandroid \
                          liblog
endif

ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) > 21 ))" )))
LOCAL_SHARED_LIBRARIES += libexif
LOCAL_C_INCLUDES += $(TOP)/external/libexif/
endif

LOCAL_SHARED_LIBRARIES += \
    libdng_sdk \
    libexpat \
    libft2 \
    libicui18n \
    libicuuc \
    libjpeg \
    liblog \
    libpiex \
    libpng \
    libheif \
    libz

LOCAL_SHARED_LIBRARIES += libhi_securec

LOCAL_SRC_FILES += esUtil.c \
                   image_decode.cpp \
                   gallery_impl.cpp

LOCAL_C_INCLUDES += \
    $(TOP)/frameworks/native/include/ \
    $(TOP)/external/jpeg/ \
    $(TOP)/external/skia/src/core \
    $(TOP)/external/skia/src/codec \
    $(TOP)/external/skia/include/core \
    $(TOP)/external/skia/include/codec \
    $(TOP)/external/webp/include/webp

LOCAL_C_INCLUDES += $(TOP)/device/hisilicon/bigfish/system/securec/securec

ifeq (STB,$(findstring STB,$(HISI_PRODUCT_LINE)))
    LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/../hal/stb/
else
    LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/../hal/dpt/
endif


LOCAL_C_INCLUDES +=  \
    $(SDK_DIR)/source/msp/api/jpeg/inc/inc_6b_android/\
    $(SDK_DIR)/source/common/include/


LOCAL_CFLAGS += -DEGL_EGLEXT_PROTOTYPES -DGL_GLEXT_PROTOTYPES -DCONFIG_HI_SDK_JPEG_VERSION -DLOG_ENABLE -Wno-unused-parameter

ifeq (STB,$(findstring STB,$(HISI_PRODUCT_LINE)))
    LOCAL_CFLAGS += -DPRODUCT_STB
else ifeq (DPT,$(findstring DPT,$(HISI_PRODUCT_LINE)))
    LOCAL_CFLAGS += -DPRODUCT_DPT
endif

LOCAL_CFLAGS += -DPLATFORM_SDK_VERSION=$(PLATFORM_SDK_VERSION)
ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) <= 19 ))" )))
    # 19
    LOCAL_CFLAGS += -DPLATFORM_VERSION_KITKAT
    ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_MODULE)
else ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) <= 22 ))" )))
    # 21 22
    LOCAL_CFLAGS += -DPLATFORM_VERSION_LOLLIPOP
else ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) <= 25 ))" )))
    # 24 25
    LOCAL_CFLAGS += -DPLATFORM_VERSION_NOUGAT
    LOCAL_CFLAGS += -Werror,-Wno-implicit-function-declaration
    LOCAL_CFLAGS += -Wno-implicit-function-declaration
else ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) <= 26 ))" )))
    # 8.0 is 26
    LOCAL_CFLAGS += -DPLATFORM_VERSION_OREO
else ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) >= 27 ))" )))
    # 8.1 and PEA is 27
    LOCAL_CFLAGS += -DPLATFORM_VERSION_O
else
    $(warning "HiGalleryL cannot detect Android_NickName from API [$(PLATFORM_SDK_VERSION)]")
endif

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/native_app_glue)
include $(call all-makefiles-under,$(LOCAL_PATH))

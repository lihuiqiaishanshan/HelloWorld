LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
include $(SDK_DIR)/Android.def

LOCAL_LDLIBS    := -lm -llog -landroid -lGLESv2 -lEGL -ljnigraphics

LOCAL_SRC_FILES:= \
        GPUPhy.cpp \
        OffScreenObject.cpp \
        WorkerThread.cpp


LOCAL_SHARED_LIBRARIES := \
    libcutils \
    libutils \
    libEGL \
    libGLESv2 \
    libui \
    libgui \
    libjnigraphics

LOCAL_SHARED_LIBRARIES += \
     libhardware \
     libhidlbase \
     libhidltransport \
     libhwbinder \
     vendor.hisilicon.hardware.hwgraphics@1.0

LOCAL_SHARED_LIBRARIES += \
    libion \
    liblog

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
    libz

LOCAL_SHARED_LIBRARIES += libhi_securec

LOCAL_STATIC_LIBRARIES := libskia

LOCAL_C_INCLUDES += \
    $(TOP)/frameworks/native/include/ \
    $(TOP)/frameworks/native/opengl/include/ \
    $(TOP)/frameworks/native/opengl/include/EGL/ \

LOCAL_C_INCLUDES += $(TOP)/system/core/libion/kernel-headers/linux

LOCAL_C_INCLUDES += \
    $(TOP)/device/hisilicon/bigfish/hardware/gpu/include \
    $(TOP)/external/skia/include/core \
    $(TOP)/external/skia/include/codec \
    $(TOP)/external/skia/src/codec \
    $(TOP)/external/skia/src/core

LOCAL_C_INCLUDES += $(TOP)/device/hisilicon/bigfish/system/securec/securec

#LOCAL_CFLAGS := -DGL_GLEXT_PROTOTYPES -DEGL_EGLEXT_PROTOTYPES -g -mfpu=neon -march=armv7-a -mtune=cortex-a8
LOCAL_CFLAGS := -DGL_GLEXT_PROTOTYPES -DEGL_EGLEXT_PROTOTYPES -g
LOCAL_CFLAGS += -DLOG_ENABLE

#before lollipop
ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) < 21 ))" )))
LOCAL_CFLAGS += -DPLATFORM_VERSION_KITKAT
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_MODULE)
else ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) < 24 ))" )))
LOCAL_CFLAGS += -DPLATFORM_VERSION_LOLLIPOP
else
LOCAL_CFLAGS += -DPLATFORM_VERSION_NOUGAT
LOCAL_CFLAGS += -Werror,-Wno-implicit-function-declaration
LOCAL_CFLAGS += -Wno-implicit-function-declaration
endif

LOCAL_CFLAGS += -DPLATFORM_SDK_VERSION=$(PLATFORM_SDK_VERSION) -DGPU_ARCH=$(GPU_ARCH)

ifeq ($(CFG_HI_TVP_SUPPORT),y)
LOCAL_CFLAGS += -DHI_TVP_SUPPORT
endif

ifeq ($(CFG_HI_SMMU_SUPPORT),y)
$(warning "building begin CFG_HI_SMMU_SUPPORT")
LOCAL_CFLAGS += -DHISILICON_SMMU
$(warning "CFG_HI_SMMU_SUPPORT=$(CFG_HI_SMMU_SUPPORT)")
endif

LOCAL_MULTILIB := 32
LOCAL_MODULE:= liboffscreenobject

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)


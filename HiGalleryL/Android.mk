LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#ifeq (1,$(filter 1,$(shell echo "$$(( $(PLATFORM_SDK_VERSION) >= 25 ))" )))
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under,src)

LOCAL_PACKAGE_NAME := HiGalleryL
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_TARGET_ARCH := arm

#LOCAL_SDK_VERSION := current
LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_JNI_SHARED_LIBRARIES := libgallerycore
LOCAL_JNI_SHARED_LIBRARIES += liboffscreenobject
LOCAL_STATIC_JAVA_LIBRARIES := gallerycore
#LOCAL_32_BIT_ONLY := true

#LOCAL_MULTILIB := 32
ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)
LOCAL_PROGUARD_ENABLED := disabled

ifeq (STB,$(findstring STB,$(HISI_PRODUCT_LINE)))
    LOCAL_CFLAGS += -DPRODUCT_STB
else ifeq (DPT,$(findstring DPT,$(HISI_PRODUCT_LINE)))
    LOCAL_CFLAGS += -DPRODUCT_DPT
endif

include $(BUILD_PACKAGE)

#include $(CLEAR_VARS)
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gallerycore:libs/GalleryCore.jar
#include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))
#endif

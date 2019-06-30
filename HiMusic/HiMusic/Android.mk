LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform


LOCAL_STATIC_JAVA_LIBRARIES := \
                                commons-io-2.4.jar

LOCAL_JNI_SHARED_LIBRARIES := libandroid_runtime

ifeq (23, $(shell echo -e "23\n$(PLATFORM_SDK_VERSION)"|sort|head -n 1))
    LOCAL_JAVA_LIBRARIES := org.apache.http.legacy
endif

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
     HiMusic/src/main/aidl/com/hisilicon/android/music/IMediaPlaybackService.aidl

LOCAL_PACKAGE_NAME := HiMusic

LOCAL_32_BIT_ONLY := true

LOCAL_PROGUARD_FLAG_FILES := HiMusic/proguard.flags
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    commons-io-2.4.jar:lib/commons-io-2.4.jar


include $(BUILD_MULTI_PREBUILT)
# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))

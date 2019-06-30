LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform


LOCAL_STATIC_JAVA_LIBRARIES := \
                                commons-io-2.6.jar
src_dirs :=HiMusic/src/main/java/
#指定Java源码路径
LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs)) \
    HiMusic/src/main/aidl/com/hisilicon/android/music/IMediaPlaybackService.aidl
source_dir:=HiMusic/src/main/
#指定Res路径
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/$(source_dir)/res
#指定清单配置文件
LOCAL_FULL_MANIFEST_FILE := $(LOCAL_PATH)/$(source_dir)/AndroidManifest.xml

LOCAL_JNI_SHARED_LIBRARIES := libandroid_runtime
LOCAL_MULTILIB := 32
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PACKAGE_NAME := HiMusic
#ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)


LOCAL_PROGUARD_FLAG_FILES := HiMusic/proguard.flags
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    commons-io-2.6.jar:HiMusic/libs/commons-io-2.6.jar

LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)

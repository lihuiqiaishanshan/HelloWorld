LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := \
                            picasso-2.5.2 \
                            android-support-v4

java_dirs :=app/src/main/java/
aidl_dirs:=app/src/main/aidl/
#指定Java源码路径
LOCAL_SRC_FILES := \
                $(call all-java-files-under, $(java_dirs)) \
                $(call all-Iaidl-files-under,$(aidl_dirs))
source_dir:=app/src/main/
#指定Res路径
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/$(source_dir)/res
#指定清单配置文件
LOCAL_FULL_MANIFEST_FILE := $(LOCAL_PATH)/$(source_dir)/AndroidManifest.xml
LOCAL_JNI_SHARED_LIBRARIES := libandroid_runtime
LOCAL_MULTILIB := 32
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PACKAGE_NAME := HiFileManager
#ALL_DEFAULT_INSTALLED_MODULES += $(LOCAL_PACKAGE_NAME)
#LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)
#导入第三方库
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := picasso-2.5.2:app/libs/picasso-2.5.2.jar
LOCAL_MODULE_TAGS := optional

include $(BUILD_MULTI_PREBUILT)

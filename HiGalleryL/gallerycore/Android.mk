
LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MULTILIB := 32
LOCAL_MODULE := gallerycore

LOCAL_SRC_FILES := $(call all-java-files-under, src)

include $(BUILD_STATIC_JAVA_LIBRARY)

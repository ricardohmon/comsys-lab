LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := comsyslab_assignment3
LOCAL_SRC_FILES := comsyslab_assignment3.c
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
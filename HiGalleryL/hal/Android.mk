LOCAL_PATH := $(call my-dir)

ifeq (STB,$(findstring STB,$(HISI_PRODUCT_LINE)))
    driver_modules := stb
else ifeq (DPT,$(findstring DPT,$(HISI_PRODUCT_LINE)))
    driver_modules := dpt
else
    $(warning "no product matching")
endif

include $(call all-named-subdir-makefiles,$(driver_modules))

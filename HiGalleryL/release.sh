#!/bin/bash

OUT_DIR=$1
SCRIPT_PATH=$2

echo "HiGalleryL release begin"
mkdir -p ${SCRIPT_PATH}/allocator/lib
cp -f ${OUT_DIR}/system/lib/libgraphicbuffallocator.so ${SCRIPT_PATH}/allocator/lib/

rm -rf ${SCRIPT_PATH}/allocator/src
mv -f ${SCRIPT_PATH}/allocator/Android.mk.rel ${SCRIPT_PATH}/allocator/Android.mk
echo "HiGalleryL release end"

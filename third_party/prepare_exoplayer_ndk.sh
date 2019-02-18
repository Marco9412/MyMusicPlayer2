#!/bin/bash

NDK_HOME=${HOME}/Android/Sdk/ndk-bundle
SDK_HOME=${HOME}/Android/Sdk
PROJECT_DIR="$(pwd)"

# 1. Generate toolchain
cd "${NDK_HOME}"
build/tools/make-standalone-toolchain.sh --toolchain=arm-linux-androideabi-4.9 --platform=android-28 --install-dir=../toolchain
cd "${PROJECT_DIR}"

# 2. ffmpeg
cd ExoPlayer/extensions/ffmpeg/src/main/jni
(git -C ffmpeg pull || git clone git://source.ffmpeg.org/ffmpeg ffmpeg)
cd ffmpeg
git checkout n4.0

# arm-v7
./configure \
--libdir=android-libs/armeabi-v7a \
--arch=arm \
--cpu=armv7-a \
--cross-prefix=${SDK_HOME}/toolchain/bin/arm-linux-androideabi- \
--sysroot="${SDK_HOME}/toolchain/sysroot" \
--extra-cflags="-march=armv7-a -mfloat-abi=softfp" \
--extra-ldflags="-Wl,--fix-cortex-a8" \
--extra-ldexeflags=-pie \
\
--target-os=android \
--disable-static \
--enable-shared \
--disable-doc \
--disable-programs \
--disable-everything \
--disable-avdevice \
--disable-avformat \
--disable-swscale \
--disable-postproc \
--disable-avfilter \
--disable-symver \
--disable-swresample \
--enable-avresample \
--enable-decoder=vorbis \
--enable-decoder=opus \
--enable-decoder=flac
make install-libs -j4

# arm v8
./configure \
    --libdir=android-libs/arm64-v8a \
    --arch=aarch64 \
    --cpu=armv8-a \
    --cross-prefix="${SDK_HOME}/toolchain/bin/aarch64-linux-android28-" \
    --cc=${SDK_HOME}/toolchain/bin/aarch64-linux-android28-clang \
    --sysroot="${HOME}/Android/Sdk/toolchain/sysroot" \
    --extra-ldexeflags=-pie \
    \
    --target-os=android \
    --disable-static \
    --enable-shared \
    --disable-doc \
    --disable-programs \
    --disable-everything \
    --disable-avdevice \
    --disable-avformat \
    --disable-swscale \
    --disable-postproc \
    --disable-avfilter \
    --disable-symver \
    --disable-swresample \
    --enable-avresample \
    --enable-decoder=vorbis \
    --enable-decoder=opus \
    --enable-decoder=flac
make install-libs -j4
cd ..

${NDK_HOME}/ndk-build APP_ABI="armeabi-v7a arm64-v8a" -j4


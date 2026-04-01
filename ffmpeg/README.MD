# FFmpeg 基础多媒体库

[![License: LGPL/GPL](https://img.shields.io/badge/License-LGPL/GPL-blue.svg)](https://www.ffmpeg.org/legal.html)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

## 📌 项目概述

本模块提供了 FFmpeg 核心动态库（`.so`）以及基础的 JNI 注册环境。作为整个项目中音视频处理的底层基石，它为上层应用提供强大的音视频编解码、转码与滤镜处理能力。

## ✨ 包含组件

本模块预置了编译好的 FFmpeg 动态链接库：
- `libavcodec.so`：音视频编解码核心
- `libavdevice.so`：输入输出设备交互
- `libavfilter.so`：丰富的滤镜与特效处理
- `libavformat.so`：多媒体封装/解封装（Mux/Demux）
- `libavutil.so`：基础工具与数据结构库
- `libswresample.so`：音频重采样
- `libswscale.so`：视频缩放与色彩空间转换
- `libffmpeg_lib.so`：JNI 封装入口库

**支持架构**：
- `armeabi-v7a`
- `arm64-v8a`

## 🔗 相关链接

- [FFmpeg 官方网站](https://www.ffmpeg.org/)
- [FFmpeg GitHub 源码](https://github.com/FFmpeg/FFmpeg)
- [FFmpeg 官方文档](http://dranger.com/ffmpeg/)

## ⚠️ 注意事项

1. **上层调用**：本模块主要作为底层依赖。具体的 FFmpeg 命令行调用及高阶协程封装，请参考 [`ffmpeg-lite-kt`](../ffmpeg-lite-kt/README.MD) 模块。
2. **构建配置**：CMakeLists 已默认配置好库链接，无需额外指定路径。

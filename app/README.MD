# Android NDK 多媒体处理示例应用 (App)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-blue.svg)](https://developer.android.com/about/versions/15)

## 📌 项目概述

本模块是 `Android_NDK_JNI` 库集合的演示与测试应用。它集中展示了如何集成并使用本项目提供的各个 NDK 模块（LAME, SoundTouch, FFmpeg, cURL, WebRTC NS 等），并提供了完整的 UI 交互界面。

## ✨ 演示功能

1. **音频编码 (LAME)**：展示 PCM 到 MP3 的实时录制与转码。
2. **实时音效 (SoundTouch)**：通过滑杆调节音频播放的速度和音调，内置多种变声预设。
3. **视频处理 (FFmpeg)**：通过 Kotlin 协程流执行 FFmpeg 命令，并实时展示处理进度。
4. **原生网络 (cURL)**：演示在 Native 层发起 HTTPS 请求及 JSON 解析，绕过 Java 层网络框架。
5. **噪音抑制 (WebRTC NS)**：提供降噪前后的音频对比测试。
6. **安全加固 (Utils)**：展示包名白名单、签名校验及反调试检测的实际效果。

## 🚀 运行要求

- **Android Studio**：Ladybug 或更高版本。
- **NDK**：r21 及以上。
- **CMake**：3.22.1+。
- **设备**：Android 5.0 (API 21) 及以上真机（推荐）。

## 🛠 开发说明

### 核心页面
- [MainActivity.kt](file:///Users/pc/Documents/Github/Android_NDK_JNI/app/src/main/java/me/shetj/sdk/ffmepg/demo/MainActivity.kt)：入口页面，集成了各功能的快速跳转与基础安全校验展示。
- [WebRtcNSActivity.kt](file:///Users/pc/Documents/Github/Android_NDK_JNI/app/src/main/java/me/shetj/sdk/ffmepg/demo/WebRtcNSActivity.kt)：降噪功能专项测试页面。

### Android 15 适配
本应用已适配 Android 15 (Target SDK 35)，并针对 16KB 内存页面大小进行了 Native 链接优化。

## 📦 依赖集成方式

本 App 模块通过 Gradle `implementation project` 方式引入了以下子模块：
```gradle
dependencies {
    implementation project(':ffmpeg')
    implementation project(':ffmpeg-lite-kt')
    implementation project(':soundTouch')
    implementation project(':tools')
    implementation project(':webrtc-ns')
    // implementation project(':lame') // 按需开启
}
```

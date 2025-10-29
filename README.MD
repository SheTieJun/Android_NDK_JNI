# Android NDK 多媒体处理库集合

[![Android](https://img.shields.io/badge/Android-API%2021+-green.svg)](https://developer.android.com/about/versions/android-5.0)
[![NDK](https://img.shields.io/badge/NDK-r21+-orange.svg)](https://developer.android.com/ndk)


## 🚀 项目简介

- 🎵 **高质量音频编码**: 基于 LAME 的 MP3 编码功能
- 🎚️ **实时音效处理**: 支持变速、变调的音频处理
- 🎬 **视频转码处理**: 基于 FFmpeg 的视频编解码
- 🌐 **网络通信**: 支持 HTTPS 请求和 JSON 数据处理
- 🔇 **噪音抑制**: WebRTC 噪音抑制模块
- 📱 **Android 15 兼容**: 支持 16KB 页面大小

## 📦 模块说明

### [LAME MP3 编码器](lame)

**功能**: 高质量的 MPEG 音频层 III (MP3) 编码器

- **官方网站**: [lame.sourceforge.io](http://lame.sourceforge.io/index.php)
- **许可证**: LGPL
- **主要特性**:
    - 支持多种比特率编码 (32-320 kbps)
    - VBR (可变比特率) 编码支持
    - 高质量音频压缩算法

### [SoundTouch 音频处理](soundTouch)

**功能**: 实时音频变速变调处理库

- **官方网站**: [surina.net/soundtouch](http://www.surina.net/soundtouch)
- **源码地址**: [codeberg.org/soundtouch](https://codeberg.org/soundtouch/soundtouch)
- **许可证**: LGPL
- **主要特性**:
    - 独立的变速和变调处理
    - 实时音频处理能力
    - 高质量的音频算法

### [FFmpeg 多媒体框架](ffmpeg)

**功能**: 完整的多媒体处理解决方案

- **官方网站**: [ffmpeg.org](https://www.ffmpeg.org/)
- **源码地址**: [github.com/FFmpeg/FFmpeg](https://github.com/FFmpeg/FFmpeg)
- **主要特性**:
    - 支持多种音视频格式
    - 强大的转码能力
    - 丰富的滤镜效果

### [网络工具集](tools)

#### cURL 网络库
- **源码地址**: [github.com/curl/curl](https://github.com/curl/curl)
- **API 文档**: [curl.se/libcurl/c](https://curl.se/libcurl/c/)
- **功能**: 高性能的 HTTP/HTTPS 网络请求库

#### OpenSSL 加密库
- **源码地址**: [github.com/openssl/openssl](https://github.com/openssl/openssl)
- **功能**: 提供 SSL/TLS 加密通信支持

#### JsonCpp JSON 解析库
- **源码地址**: [github.com/open-source-parsers/jsoncpp](https://github.com/open-source-parsers/jsoncpp)
- **API 文档**: [jsoncpp-docs](http://open-source-parsers.github.io/jsoncpp-docs/doxygen/index.html)
- **功能**: 高效的 C++ JSON 数据解析库

### [WebRTC 噪音抑制](webrtc-ns)

**功能**: 实时音频噪音抑制模块

- **官方网站**: [webrtc.org](https://webrtc.org/)
- **源码地址**: [WebRTC NS Module](https://webrtc.googlesource.com/src/+/refs/heads/main/modules/audio_processing/ns)
- **主要特性**:
    - 实时噪音检测和抑制
    - 低延迟处理
    - 高质量的音频输出


## 📚 使用说明

### LAME 使用示例

```kotlin
// 初始化 LAME 编码器
val lameUtils = LameUtils()
lameUtils.init(44100, 2, 128) // 采样率, 声道数, 比特率

// 编码音频数据
val mp3Buffer = ByteArray(1024)
val result = lameUtils.encode(audioData, audioData, mp3Buffer)

// 完成编码
lameUtils.destroy()
```

### SoundTouch 使用示例

```kotlin
// 创建 SoundTouch 实例
val soundTouch = SoundTouch()
soundTouch.setSampleRate(44100)
soundTouch.setChannels(2)

// 设置变速和变调
soundTouch.setTempo(1.2f)  // 提速 20%
soundTouch.setPitch(1.1f)  // 升调 10%

// 处理音频数据
soundTouch.putSamples(inputBuffer, numSamples)
val outputSamples = soundTouch.receiveSamples(outputBuffer, maxSamples)
```

## ⚙️ 配置参数

### Android 15 兼容性配置

为了兼容 Android 15 的 16KB 页面大小，需要在 CMakeLists.txt 中添加以下配置：

```cmake
# CMake 3.13 之前
target_link_libraries(your_target "-Wl,-z,max-page-size=16384")

# CMake 3.13 及以后版本
target_link_options(your_target PRIVATE "-Wl,-z,max-page-size=16384")
```


## ❓ 常见问题

### Q: 编译时出现 NDK 版本不兼容问题？
**A**: 请确保使用 NDK r21 或更高版本，并在 `build.gradle` 中正确配置 `ndkVersion`。

### Q: 在 Android 15 设备上崩溃？
**A**: 请确保在 CMakeLists.txt 中添加了 16KB 页面大小的链接选项。

### Q: 音频处理延迟过高？
**A**: 建议调整音频缓冲区大小，使用较小的缓冲区可以降低延迟。

### Q: 如何优化库的大小？
**A**: 可以通过以下方式优化：
- 只编译需要的架构 (如仅保留 arm64-v8a)
- 移除不需要的功能模块
- 启用编译器优化选项

### Q: 支持哪些音频格式？
**A**:
- **输入格式**: PCM, WAV
- **输出格式**: MP3 (通过 LAME), 各种格式 (通过 FFmpeg)

## 🔗 相关链接
- [Android NDK 官方文档](https://developer.android.com/ndk)
- [CMake 官方文档](https://cmake.org/documentation/)
 


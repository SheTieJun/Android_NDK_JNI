# Android NDK JNI 项目 Code Wiki

## 1. 项目简介
本项目（`Android_NDK_JNI`）是一个 Android NDK 多媒体处理和网络工具的集合库。旨在提供基于 JNI/C++ 的高性能底层能力，主要包括高质量音频编码（LAME）、实时音效处理（SoundTouch）、音视频编解码（FFmpeg）、网络通信（cURL）以及噪音抑制（WebRTC NS）。

## 2. 项目整体架构
项目采用模块化设计，分为上层（应用/Demo展示）与底层（功能模块库）两部分，通过 JNI 接口连接 Kotlin/Java 代码与 C/C++ 原生代码。

主要层次划分如下：
- **UI/业务层**：`app` 模块，提供测试和调用各 NDK 模块的示例。
- **JNI 封装层**：各模块的 Kotlin 对象封装（如 `LameUtils.kt`、`CUrlKit.kt`），负责声明 `external` 方法并加载 `.so` 库。
- **C/C++ 实现层**：通过 CMake 编译，包含各开源库的源码或预编译的动态库（如 FFmpeg 的 `libavcodec.so`、WebRTC 源码等），处理实际业务逻辑。

## 3. 主要模块职责

### `app` 模块
- **职责**：项目的主入口和 Demo 应用。
- **功能**：展示如何调用 LAME 进行 MP3 编码、如何使用 SoundTouch 变调、演示 cURL 网络请求、以及 WebRTC 降噪等功能。

### `lame` 模块
- **职责**：音频 MP3 编码器。
- **功能**：集成 LAME (MPEG Audio Layer III) 库，提供高质量的 PCM 到 MP3 的转码，支持 VBR、CBR，以及低通/高通滤波器设置。

### `soundTouch` 模块
- **职责**：实时音频处理。
- **功能**：集成 SoundTouch 开源库，实现音频的独立变速（Tempo）和变调（Pitch），支持如“男声”、“女声”、“机器人”等特定音效的实时转换。

### `ffmpeg` & `ffmpeg-lite-kt` 模块
- **职责**：音视频编解码处理框架。
- **功能**：
  - `ffmpeg`：提供基础的 FFmpeg 动态库和原生支持。
  - `ffmpeg-lite-kt`：基于协程/Rx 封装的 FFmpeg 命令执行器，能够以响应式流 (`runCommandFlow`) 的方式执行复杂的音视频处理命令，监听进度、成功与失败状态。

### `tools` 模块
- **职责**：原生网络与数据解析工具。
- **功能**：
  - 基于 NDK `prefab` 特性引入 `curl` 和 `openssl`，提供比系统更底层、独立的高性能 HTTP/HTTPS 客户端。
  - 引入 `jsoncpp`，提供 C++ 层的 JSON 快速解析能力。

### `webrtc-ns` 模块
- **职责**：音频降噪。
- **功能**：提取并封装了 WebRTC 的核心 Noise Suppression (NS) 模块，支持针对不同分贝（6dB~21dB）的实时环境噪音抑制处理。

## 4. 关键类与函数说明

### 4.1 LameUtils ([lame/src/main/java/me/shetj/ndk/lame/LameUtils.kt](file:///Users/pc/Documents/Github/Android_NDK_JNI/lame/src/main/java/me/shetj/ndk/lame/LameUtils.kt))
- `init(...)`: 初始化 LAME 编码器参数（采样率、声道、比特率、质量、VBR等）。
- `encode(...) / encodeInterleaved(...)`: 核心编码方法，将 PCM `ShortArray` 数据编码为 MP3 `ByteArray`。
- `flush(mp3buf)`: 刷新并输出缓冲区中剩余的 MP3 数据。
- `close()`: 释放原生层的内存与资源。

### 4.2 SoundTouch ([soundTouch/src/main/java/me/shetj/ndk/soundtouch/SoundTouch.kt](file:///Users/pc/Documents/Github/Android_NDK_JNI/soundTouch/src/main/java/me/shetj/ndk/soundtouch/SoundTouch.kt))
- `newInstance()`: 创建 C++ `SoundTouch` 对象的句柄（返回 Long 指针）。
- `init(...)`: 初始化采样率、声道及初始速度/音调。
- `setTempo(...) / setPitchSemiTones(...)`: 设置音频节拍或音调（半音阶），可实现特定人声效果（如 `applyRobotVoiceEffect`）。
- `putSamples(...)`: 将原始音频数据输入到 SoundTouch 的处理管道中。
- `receiveSamples(...)`: 循环读取处理后改变了音调/速度的音频流。

### 4.3 CUrlKit ([tools/src/main/java/me/shetj/sdk/curl/CUrlKit.kt](file:///Users/pc/Documents/Github/Android_NDK_JNI/tools/src/main/java/me/shetj/sdk/curl/CUrlKit.kt))
- `init() / cleanup()`: 全局初始化与释放 cURL 资源。
- `get(url) / post(url, body)`: 提供快捷的 HTTP 请求方法。
- `request(...)`: 高级请求方法，支持配置 Header、Timeout、Cookie、重定向、证书等。

### 4.4 WebRtcNs ([webrtc-ns/src/main/cpp/ns.cpp](file:///Users/pc/Documents/Github/Android_NDK_JNI/webrtc-ns/src/main/cpp/ns.cpp) 及对应 Java 封装)
- `webRtcNsCreate(...)`: 根据指定的采样率、声道和降噪等级（0~3，对应 6~21dB）初始化 `NoiseSuppressor`。
- `noiseSuppressionByShort(...)`: 传入带噪音的音频数据数组，执行分析 (`Analyze`) 并降噪处理 (`Process`)。

### 4.5 FFmpegKit ([ffmpeg-lite-kt/src/main/java/me/shetj/ffmpeg/FFmpegKit.kt](file:///Users/pc/Documents/Github/Android_NDK_JNI/ffmpeg-lite-kt/src/main/java/me/shetj/ffmpeg/FFmpegKit.kt))
- `runCommandFlow(command)`: 以 Kotlin 协程 `Flow` 返回 FFmpeg 命令的执行状态 (`OnStart`, `OnProgress`, `OnFinish`, `OnError`)。

## 5. 依赖关系
- **模块依赖（内部）**：
  `app` 模块同时依赖并整合了 `ffmpeg`, `soundTouch`, `lame` (按需), `tools`, `ffmpeg-lite-kt`, `webrtc-ns`。
- **三方原生库依赖（外部）**：
  - `tools` 模块依赖 NDK 的预编译包 (Prefab): `com.android.ndk.thirdparty:curl:7.69.1-alpha-1`, `jsoncpp`, `openssl`。
  - `ffmpeg` 包含预编译好的多架构 `.so` 文件（如 `libavcodec.so`, `libavformat.so` 等）。

## 6. 项目运行方式
1. **环境准备**：
   - Android Studio (推荐较新版本，以支持 CMake 3.22.1 以上)。
   - Android NDK (推荐 r21+)，并确保已在 SDK Manager 中下载对应的 NDK 和 CMake 工具。
2. **构建与运行**：
   - 使用 Git 克隆本仓库到本地。
   - 在 Android Studio 中打开项目，等待 Gradle 同步完成。
   - 在 `local.properties` 或 `gradle.properties` 中确认 NDK 路径配置正确。
   - 点击 **Run 'app'** 即可在模拟器或真机（支持 `armeabi-v7a`、`arm64-v8a` 等架构）上安装测试。
3. **Android 15 兼容说明**：
   - 项目已经在 CMakeLists 中为原生库配置了 `-Wl,-z,max-page-size=16384` 链接选项，以兼容 Android 15 要求支持的 16KB 内存页面大小。

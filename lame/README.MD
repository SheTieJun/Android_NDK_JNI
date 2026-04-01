# LAME MP3 Encoder for Android

[![License: LGPL](https://img.shields.io/badge/License-LGPL-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

## 📌 项目概述

本项目是基于 [LAME](https://lame.sourceforge.io/index.php) 的 Android NDK 封装库，提供高质量的 PCM 音频到 MP3 格式的编码功能。核心逻辑通过 JNI 实现，保证了转码性能与效率。LAME 是一个高质量的 MPEG 音频层 III (MP3) 编码器，在 LGPL 许可证下发布。

### ✨ 主要功能特性

- **高质量音频编码**：基于 LAME 引擎，提供专业级的 MP3 编码质量。
- **多种编码模式**：支持 CBR（固定比特率）和 VBR（可变比特率）编码。
- **灵活的音频参数**：支持自定义采样率、比特率、声道数等参数。
- **音频滤波器**：内置低通和高通滤波器，优化音频质量。
- **实时编码**：支持实时 PCM 数据流编码。
- **音频分析**：提供 PCM 音频分贝值计算功能。

## 🚀 快速开始

### 1. 初始化编码器

```kotlin
import me.shetj.ndk.lame.LameUtils

// 基本初始化
LameUtils.init(
    inSampleRate = 44100,    // 输入采样率
    inChannel = 2,           // 输入声道数（1=单声道，2=立体声）
    outSampleRate = 44100,   // 输出采样率
    outBitrate = 128,        // 输出比特率（kbps）
    quality = 2,             // 编码质量（0-9，0最高质量）
    lowpassFreq = -1,        // 低通滤波器频率（-1=不使用）
    highpassFreq = -1,       // 高通滤波器频率（-1=不使用）
    vbr = false,             // 是否使用VBR模式
    enableLog = true         // 是否启用日志输出
)
```

### 2. 编码音频数据

**单声道编码：**
```kotlin
// 填充PCM数据...
val bytesEncoded = LameUtils.encode(
    bufferLeft = pcmBuffer,
    bufferRight = pcmBuffer,  // 单声道时左右声道使用相同数据
    samples = pcmBuffer.size,
    mp3buf = mp3Buffer
)
```

**立体声交错编码（推荐）：**
```kotlin
// 填充交错的PCM数据（左右声道交替）...
val bytesEncoded = LameUtils.encodeInterleaved(
    pcm = interleavedPcm,
    samples = interleavedPcm.size / 2,  // 样本数 = 数组长度 / 2
    mp3buf = mp3Buffer
)
```

### 3. 完成编码并释放资源

```kotlin
// 刷新缓冲区，获取剩余的编码数据
val finalBytes = LameUtils.flush(finalBuffer)

// 必须调用，关闭编码器，释放 C++ 内存资源
LameUtils.close()
```

## 🛠 高级功能

### VBR 模式编码

```kotlin
LameUtils.init(
    /* ...其他参数... */
    vbr = true,              // 启用VBR
)

// VBR模式需要写入VBR头信息
val outputFile = "/path/to/output.mp3"
LameUtils.writeVBRHeader(outputFile)
```

### 音频分析

```kotlin
val pcmData = ShortArray(1024)
// 获取音频分贝值
val dbValue = LameUtils.getPCMDB(pcmData, pcmData.size)
println("当前音频分贝值: ${dbValue}dB")
```

## ⚠️ 注意事项

1. **资源管理**：使用完毕后必须调用 `LameUtils.close()` 释放资源，防止 Native 内存泄漏。
2. **线程安全**：`LameUtils` 不是线程安全的，多线程使用需要外部同步。
3. **缓冲区大小**：MP3 输出缓冲区建议至少为输入 PCM 数据的 1.25 倍。
4. **VBR 模式**：使用 VBR 时必须调用 `writeVBRHeader()` 写入正确的头信息。
5. **采样率限制**：支持的采样率范围为 8000-48000 Hz。

# SoundTouch 音频处理库

[![License: LGPL](https://img.shields.io/badge/License-LGPL-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

## 📌 项目概述

SoundTouch 是一个开源的音频处理库，专门用于音频的变速、变调处理。本项目提供了 SoundTouch C++ 库 (v2.3.1) 的 Android JNI 封装，支持实时音频流处理和文件批处理。

- **官网**：[http://www.surina.net/soundtouch](http://www.surina.net/soundtouch)
- **源码**：[https://codeberg.org/soundtouch/soundtouch](https://codeberg.org/soundtouch/soundtouch)

## ✨ 主要功能

- **音频变速 (Tempo)**：改变播放速度而不影响音调。
- **音频变调 (Pitch)**：改变音调而不影响播放速度。
- **组合处理 (Rate)**：同时调整速度和音调。
- **实时处理**：支持音频流的实时分块处理。
- **文件处理**：支持 WAV 文件的批量处理。
- **内置音效**：提供快速应用的男声、女声、机器人、儿童等变声效果。

## 🚀 快速开始

### 1. 实例初始化

```kotlin
val soundTouch = SoundTouch()
val handle = soundTouch.newInstance()

// 基础初始化：立体声，44.1kHz，正常速度，不变调
soundTouch.init(handle, channels = 2, sampleRate = 44100, tempo = 1.0f, pitch = 0.0f, speed = 1.0f)
```

### 2. 速度与音调控制

```kotlin
// 使用半音调整音调，范围 [-12.0, 12.0]
soundTouch.setPitchSemiTones(handle, 4.0f)  // 升高4个半音，类似女声

// 设置播放节拍倍率（不影响音调）
soundTouch.setTempo(handle, 1.2f) // 1.2倍速播放

// 使用内置的快捷变声效果
soundTouch.applyRobotVoiceEffect(handle) // 机器人效果
```

### 3. 音频处理

**处理音频流（适用于录音、播放器等实时场景）：**
```kotlin
val inputBuffer = ShortArray(1024)
// ... 填充原始音频 PCM 数据
soundTouch.putSamples(handle, inputBuffer, inputBuffer.size)

val outputBuffer = ShortArray(1024)
var samplesReceived: Int
do {
    // 循环接收处理后的音频数据
    samplesReceived = soundTouch.receiveSamples(handle, outputBuffer)
    if (samplesReceived > 0) {
        // 播放或保存 outputBuffer 中的数据
    }
} while (samplesReceived > 0)
```

**处理本地 WAV 文件：**
```kotlin
val result = soundTouch.processFile(handle, "/path/input.wav", "/path/output.wav")
```

### 4. 结束与释放

```kotlin
val finalBuffer = ShortArray(1024)
soundTouch.flush(handle, finalBuffer) // 获取剩余数据
soundTouch.deleteInstance(handle)     // 必须调用，释放 C++ 对象
```

## 💡 最佳实践与参数推荐

### 自然变声效果
- **轻微男声**：`setPitchSemiTones(handle, -2.0f)`
- **轻微女声**：`setPitchSemiTones(handle, 3.0f)`
- **明显男声**：`setPitchSemiTones(handle, -4.0f)`
- **明显女声**：`setPitchSemiTones(handle, 5.0f)`

### 性能优化建议
1. **缓冲区大小**：推荐缓冲区大小设置为 1024-4096 采样点，过大可能增加延迟，过小可能影响性能。
2. **实例复用**：避免频繁创建和销毁 `SoundTouch` 实例，复用句柄并动态调整参数即可。
3. **参数范围**：严格遵守各参数的有效范围（如半音 [-12, 12]），超出范围可能导致音质严重失真。
4. **内存管理**：使用完毕务必调用 `deleteInstance`。

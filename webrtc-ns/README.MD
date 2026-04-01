# WebRTC Noise Suppression (NS) for Android

[![License](https://img.shields.io/badge/License-WebRTC-blue.svg)](https://webrtc.googlesource.com/src/+/refs/heads/main/LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

## 📌 项目概述

本模块提取自 [WebRTC](https://webrtc.org/) 的音频处理引擎，专门用于实时音频噪音抑制（Noise Suppression）。通过 JNI 封装，开发者可以在 Android 应用中轻松集成这一高性能的降噪算法。

## ✨ 主要功能

- **实时降噪**：针对 PCM 音频流进行低延迟的实时噪音消除。
- **多级抑制**：支持四种不同的抑制强度级别。
- **高质量输出**：在消除环境噪音的同时，尽可能保留人声的自然度。
- **多声道支持**：支持单声道及多声道音频处理。

## 🚀 快速开始

### 1. 初始化降噪器

```kotlin
// 创建降噪器实例
// sampleRate: 采样率 (如 16000, 44100)
// channels: 声道数
// level: 降噪等级 (0-3，级别越高抑制越强)
val nsHandler = WebRtcNs.webRtcNsCreate(sampleRate, channels, level)

// 创建必要的辅助缓冲区
val abHandler = WebRtcNs.createAudioBuffer(sampleRate, channels)
val scHandler = WebRtcNs.createStreamConfig(sampleRate, channels)
```

### 2. 处理音频数据

**处理 ShortArray 数据：**
```kotlin
val audioData = ShortArray(frameSize)
// ... 填充原始音频数据
WebRtcNs.noiseSuppressionByShort(nsHandler, abHandler, scHandler, audioData)
// 处理后的 audioData 即为降噪后的数据
```

### 3. 释放资源

```kotlin
// 必须手动释放 C++ 层分配的内存
WebRtcNs.webRtcNsFree(nsHandler, abHandler, scHandler)
```

## ⚙️ 参数说明

### 降噪等级 (Level)
- **0 (k6dB)**：轻微抑制，保留更多细节。
- **1 (k12dB)**：中等抑制。
- **2 (k18dB)**：强力抑制。
- **3 (k21dB)**：极强抑制，适用于极嘈杂环境。

## ⚠️ 注意事项

1. **帧大小限制**：WebRTC NS 算法通常以 10ms 为一个处理周期。例如在 16000Hz 采样率下，单次处理的样本数应为 160 个。
2. **采样率支持**：支持 8000Hz, 16000Hz, 32000Hz, 44100Hz, 48000Hz 等标准采样率。对于高于 16000Hz 的采样率，算法内部会自动进行分带处理。
3. **资源管理**：由于涉及 C++ `new` 操作，必须确保在销毁时调用 `webRtcNsFree` 以避免内存泄漏。

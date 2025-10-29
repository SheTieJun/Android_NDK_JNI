# LAME MP3 Encoder for Android

[![License: LGPL](https://img.shields.io/badge/License-LGPL-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

## 项目概述

本项目是基于 [LAME](https://lame.sourceforge.io/index.php) 的 Android NDK 封装库，提供高质量的 PCM 音频到 MP3 格式的编码功能。LAME 是一个高质量的 MPEG 音频层 III (MP3) 编码器，在 LGPL 许可证下发布。

### 主要功能

- **高质量音频编码**：基于 LAME 引擎，提供专业级的 MP3 编码质量
- **多种编码模式**：支持 CBR（固定比特率）和 VBR（可变比特率）编码
- **灵活的音频参数**：支持自定义采样率、比特率、声道数等参数
- **音频滤波器**：内置低通和高通滤波器，优化音频质量
- **实时编码**：支持实时 PCM 数据流编码
- **音频分析**：提供 PCM 音频分贝值计算功能

### 相关项目

- [Mp3Recorder](https://github.com/SheTieJun/Mp3Recorder) - 基于本库的录音应用示例

## 快速开始

### 基本使用

#### 1. 初始化编码器

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

#### 2. 编码音频数据

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

**立体声编码：**
```kotlin

// 填充交错的PCM数据（左右声道交替）...
val bytesEncoded = LameUtils.encodeInterleaved(
    pcm = interleavedPcm,
    samples = interleavedPcm.size / 2,  // 样本数 = 数组长度 / 2
    mp3buf = mp3Buffer
)
```

#### 3. 完成编码

```kotlin
// 刷新缓冲区，获取剩余的编码数据
val finalBytes = LameUtils.flush(finalBuffer)

// 关闭编码器，释放资源
LameUtils.close()
```

### 高级功能

#### VBR 模式编码

```kotlin
// 启用VBR模式的初始化
LameUtils.init(
    inSampleRate = 44100,
    inChannel = 2,
    outSampleRate = 44100,
    outBitrate = 128,        // VBR模式下作为平均比特率
    quality = 2,
    lowpassFreq = -1,
    highpassFreq = -1,
    vbr = true,              // 启用VBR
    enableLog = true
)

// VBR模式需要写入VBR头信息
val outputFile = "/path/to/output.mp3"
LameUtils.writeVBRHeader(outputFile)
```

#### 音频滤波器使用

```kotlin
// 使用滤波器优化音质
LameUtils.init(
    inSampleRate = 44100,
    inChannel = 2,
    outSampleRate = 44100,
    outBitrate = 128,
    quality = 2,
    lowpassFreq = 15000,     // 低通滤波器：截止15kHz以上频率
    highpassFreq = 80,       // 高通滤波器：截止80Hz以下频率
    vbr = false,
    enableLog = true
)
```

#### 音频分析

```kotlin
val pcmData = ShortArray(1024)
// 填充PCM数据...

// 获取音频分贝值
val dbValue = LameUtils.getPCMDB(pcmData, pcmData.size)
println("当前音频分贝值: ${dbValue}dB")
```

## API 参考

### 编码质量参数

| 质量等级 | 说明 | 适用场景 |
|---------|------|----------|
| 0 | 最高质量，最慢速度 | 专业音频制作 |
| 2 | 高质量（推荐） | 一般音频应用 |
| 5 | 标准质量 | 实时编码 |
| 9 | 最低质量，最快速度 | 语音录制 |

### 比特率建议

| 音频类型 | 推荐比特率 | 说明 |
|---------|-----------|------|
| 语音录制 | 64-96 kbps | 节省存储空间 |
| 音乐录制 | 128-192 kbps | 平衡质量与文件大小 |
| 高保真音乐 | 256-320 kbps | 最佳音质 |

### 错误码说明

编码方法返回值含义：
- `> 0`: 成功编码的字节数
- `0`: 没有输出数据（正常情况）
- `-1`: MP3 缓冲区太小
- `-2`: 内存分配问题
- `-3`: 编码器未正确初始化
- `-4`: 心理声学模型问题

## 注意事项

⚠️ **重要提醒**

1. **资源管理**：使用完毕后必须调用 `LameUtils.close()` 释放资源
2. **线程安全**：LameUtils 不是线程安全的，多线程使用需要外部同步
3. **缓冲区大小**：MP3 输出缓冲区建议至少为输入 PCM 数据的 1.25 倍
4. **VBR 模式**：使用 VBR 时必须调用 `writeVBRHeader()` 写入正确的头信息
5. **采样率限制**：支持的采样率范围为 8000-48000 Hz

## 性能优化建议

1. **批量处理**：尽量使用较大的缓冲区进行批量编码，减少 JNI 调用开销
2. **内存复用**：重复使用相同大小的缓冲区，避免频繁内存分配
3. **参数选择**：根据应用场景选择合适的质量等级和比特率
4. **异步处理**：在后台线程进行编码操作，避免阻塞 UI 线程

## 常见问题

### Q: 编码后的 MP3 文件无法播放？
A: 检查是否正确调用了 `flush()` 方法，并确保 VBR 模式下调用了 `writeVBRHeader()`。

### Q: 编码速度太慢？
A: 尝试降低质量等级（增加 quality 值）或使用较低的比特率。

### Q: 音质不满意？
A: 尝试提高比特率、降低质量等级（减少 quality 值）或启用适当的滤波器。

## 版本历史

### v1.0.0
- 初始版本发布
- 支持基本的 PCM 到 MP3 编码功能
- 支持 CBR 和 VBR 编码模式

## 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发环境要求
- Android Studio 4.0+
- NDK 21+
- CMake 3.10+

### 提交规范
1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目基于 LGPL 许可证发布。详见 [LICENSE](LICENSE) 文件。

## 相关链接

- [LAME 官网](https://lame.sourceforge.io/index.php)
- [LAME 详细参数说明](https://doorxp.github.io/apple/2012/07/10/Lame-Detail-Args.html)
- [LAME 使用文档](https://www.jianshu.com/p/5208d6dcd7eb)

---

如有问题或建议，请提交 [Issue](../../issues) 或联系维护者。
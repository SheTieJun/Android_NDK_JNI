# FFmpegLite-Kt (Kotlin 协程封装)

[![Kotlin](https://img.shields.io/badge/Kotlin-Coroutines-blue.svg)](https://kotlinlang.org/docs/coroutines-overview.html)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

## 📌 项目概述

本模块依赖底层的 `ffmpeg` 模块，并基于 Kotlin 协程（Coroutines）和数据流（Flow）提供了非阻塞式的 FFmpeg 命令行执行封装。这极大地方便了在 Android 开发中执行复杂的音视频处理任务，如视频转码、裁剪、加水印、音频提取等。

底层动态库基于 [RxFFmpeg](https://github.com/microshow/RxFFmpeg) 的 C/C++ 封装进行适配与改良。

## ✨ 核心特性

- **协程支持**：基于 Kotlin Coroutines `1.6.4+`，无需手动管理线程，完美契合现代异步开发。
- **状态流监听**：通过 `Flow` 实时监听命令的执行状态（开始、进度更新、完成、错误、取消）。
- **命令构建器**：提供清晰语义的 DSL 构建器，方便拼接和维护复杂的 FFmpeg 命令。

## 🚀 快速开始

### 1. 基础协程调用

直接挂起执行并等待结果返回。

```kotlin
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.shetj.ffmpeg.FFmpegKit
import me.shetj.ffmpeg.convertToCommand

lifecycleScope.launch {
    // 字符串转换为命令数组
    val command = "ffmpeg -y -i input.mp4 -o output.gif".convertToCommand()
    // 挂起函数，执行完毕后返回
    val result = FFmpegKit.runCommand(command)
}
```

### 2. 使用 Flow 监听执行状态 (推荐)

对于耗时较长的音视频处理，通常需要向用户展示进度。

```kotlin
lifecycleScope.launch {
    val command = "ffmpeg -y -i input.mp4 -o output.gif".convertToCommand()
    
    FFmpegKit.runCommandFlow(command).collect { state ->
        when (state) {
            FFmpegState.OnStart -> {
                Log.d("FFmpegKit", "开始执行")
            }
            is FFmpegState.OnProgress -> {
                // state.progress 可以获取进度信息
                Log.d("FFmpegKit", "进度更新...")
            }
            FFmpegState.OnFinish -> {
                Log.d("FFmpegKit", "执行完成")
            }
            is FFmpegState.OnError -> {
                Log.e("FFmpegKit", "发生错误: ${state.message}")
            }
            FFmpegState.OnCancel -> {
                Log.w("FFmpegKit", "任务被取消")
            }
        }
    }
}
```

### 3. 使用 DSL 命令构建器

处理复杂命令时，使用构建器可以提高代码可读性，避免空格拼接错误。

```kotlin
import me.shetj.ffmpeg.buildCommand

val command = buildCommand {
    append("-y")                             // 覆盖输出
    append("-i")
    append("/storage/emulated/0/input.mp4")  // 输入文件
    append("-vf")
    append("boxblur=5:1")                    // 添加模糊滤镜
    append("-preset")
    append("superfast")                      // 编码预设
    append("/storage/emulated/0/result.mp4") // 输出文件
}
```

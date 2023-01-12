## FFmpegKit

FFmpeg Kotlin 协程封装

- FFmpeg的`so`使用的是[RxFFmpeg](https://github.com/microshow/RxFFmpeg)
- Kotlin coroutines `1.6.4`

## How TO USE

### 1.use coroutines

```kotlin
lifecycleScope.launch {
    val command = "ffmpeg -y -i input.mp4 -o output.gif".convertToCommand()
    val result = FFmpegKit.runCommand(command)
}
```
###  2.use coroutines :Flow
```kotlin
lifecycleScope.launch {
    val command = "ffmpeg -y -i input.mp4 -o output.gif".convertToCommand()
    FFmpegKit.runCommandFlow(command).collect {
        when (it) {
            FFmpegState.OnCancel -> {
                Log.e("FFmpegKit", "OnCancel")
            }
            is FFmpegState.OnError -> {
                Log.e("FFmpegKit", it.message.toString())
            }
            FFmpegState.OnFinish -> {
                Log.e("FFmpegKit", "OnFinish")
            }
            is FFmpegState.OnProgress -> {
                Log.e("FFmpegKit", "OnProgress")
            }
            FFmpegState.OnStart -> {
                Log.e("FFmpegKit", "OnStart")
            }
        }
    }
}
```

##  Command Build
```kotlin
val command = buildCommand {
        append("-i");
        append("/storage/emulated/0/1/input.mp4");
        append("-vf");
        append("boxblur=5:1");
        append("-preset");
        append("superfast");
        append("/storage/emulated/0/1/result.mp4");
}
```

```kotlin


```
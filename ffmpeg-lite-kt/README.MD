## FFmpegKit

FFmpeg Kotlin 协程封装

## How TO USE

```kotlin
    lifecycleScope.launch {
    val command = "ffmepg -version".convertToCommand()
    withContext(Dispatchers.IO) {
        FFmpegKit.runCommand(command).collect {
            when (it) {
                RunState.OnCancel -> {
                    Log.e("FFmpegKit", "OnCancel")
                }
                is RunState.OnError -> {
                    Log.e("FFmpegKit", it.message.toString())
                }
                RunState.OnFinish -> {
                    Log.e("FFmpegKit", "OnFinish")
                }
                is RunState.OnProgress -> {
                    Log.e("FFmpegKit", "OnProgress")
                }
                RunState.OnStart -> {
                    Log.e("FFmpegKit", "OnStart")
                }
            }
        }
    }
}
```
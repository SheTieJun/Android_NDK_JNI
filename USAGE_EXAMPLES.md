# Android NDK JNI 项目使用案例指南

本文档提供了本项目中各个核心模块的典型使用案例，帮助开发者快速理解和集成这些功能。

---

## 1. LAME: 并发音频转码 (PCM 转 MP3)

在最新的重构中，`LameUtils` 被改为了一个普通的 `class`，这允许我们实例化多个编码器，在不同的协程或线程中**同时**进行多路音频转码，互相之间不再干扰。

### 1.1 基本并发转码示例

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shetj.ndk.lame.LameUtils
import java.io.File

suspend fun convertMultipleAudioToMp3(pcmFiles: List<File>, outputDir: File) {
    withContext(Dispatchers.IO) {
        pcmFiles.forEachIndexed { index, pcmFile ->
            launch {
                // 每个协程创建一个独立的 LameUtils 实例
                val lameUtils = LameUtils()
                val outputFile = File(outputDir, "output_${index}.mp3")
                
                try {
                    // 1. 初始化独立的编码器上下文
                    lameUtils.init(
                        inSampleRate = 44100,
                        inChannel = 2,           // 假设输入是双声道
                        outSampleRate = 44100,
                        outBitrate = 128,
                        quality = 2,
                        lowpassFreq = -1,
                        highpassFreq = -1,
                        vbr = false,
                        enableLog = true
                    )

                    // 2. 模拟读取 PCM 并交错编码
                    val pcmData = readPcmData(pcmFile) // 自定义方法：读取 PCM 到 ShortArray
                    val mp3Buffer = ByteArray(pcmData.size) // 建议大小：PCM大小的 1.25 倍以上

                    val encodedSize = lameUtils.encodeInterleaved(
                        pcm = pcmData,
                        samples = pcmData.size / 2, // 双声道，样本数是数组大小的一半
                        mp3buf = mp3Buffer
                    )

                    if (encodedSize > 0) {
                        outputFile.appendBytes(mp3Buffer.copyOf(encodedSize))
                    }

                    // 3. 刷新缓冲区，写入文件末尾
                    val finalBuffer = ByteArray(1024)
                    val flushSize = lameUtils.flush(finalBuffer)
                    if (flushSize > 0) {
                        outputFile.appendBytes(finalBuffer.copyOf(flushSize))
                    }

                } finally {
                    // 4. 确保释放底层的 C++ 资源
                    lameUtils.close()
                }
            }
        }
    }
}
```

---

## 2. SoundTouch: 实时变声处理

SoundTouch 可以单独改变音频的速度或音调。它同样使用实例句柄来管理状态，非常适合用于音频播放器或者变声录音机中。

### 2.1 将录音流转换为“女声”

```kotlin
import me.shetj.ndk.soundtouch.SoundTouch

class VoiceChanger {
    private val soundTouch = SoundTouch()
    private var handle: Long = 0

    fun start() {
        // 1. 创建实例句柄
        handle = soundTouch.newInstance()
        
        // 2. 初始化参数 (立体声, 44.1kHz)
        soundTouch.init(handle, channels = 2, sampleRate = 44100, tempo = 1.0f, pitch = 0.0f, speed = 1.0f)
        
        // 3. 应用变声效果（升高音调模拟女声）
        soundTouch.setPitchSemiTones(handle, 5.0f)
    }

    fun processAudioStream(inputPcm: ShortArray): ShortArray {
        // 4. 输入原始音频数据
        soundTouch.putSamples(handle, inputPcm, inputPcm.size / 2) // len 是采样点数

        // 5. 循环接收处理后的数据
        var totalReceived = 0
        val tempBuffer = ShortArray(4096)
        val resultList = mutableListOf<Short>()

        do {
            val received = soundTouch.receiveSamples(handle, tempBuffer)
            if (received > 0) {
                // 立体声，实际数组长度是采样点数 * 2
                resultList.addAll(tempBuffer.take(received * 2))
                totalReceived += received
            }
        } while (received > 0)

        return resultList.toShortArray()
    }

    fun stop() {
        // 6. 释放资源
        if (handle != 0L) {
            soundTouch.deleteInstance(handle)
            handle = 0L
        }
    }
}
```

---

## 3. FFmpeg-lite-kt: 协程化视频处理

使用 `FFmpegKit` 可以通过 Kotlin 的 Flow 轻松监听视频处理进度，而不需要编写复杂的回调接口。

### 3.1 提取视频中的音频为 MP3

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.shetj.ffmpeg.FFmpegKit
import me.shetj.ffmpeg.FFmpegState
import me.shetj.ffmpeg.buildCommand

class VideoProcessViewModel : ViewModel() {

    fun extractAudio(videoPath: String, outputMp3Path: String) {
        viewModelScope.launch {
            // 1. 使用 DSL 构建 FFmpeg 命令
            val command = buildCommand {
                append("-i")
                append(videoPath)       // 输入视频
                append("-vn")           // 禁用视频流
                append("-acodec")
                append("libmp3lame")    // 指定 mp3 编码器
                append("-y")            // 覆盖输出文件
                append(outputMp3Path)   // 输出文件
            }

            // 2. 执行命令并收集状态流
            FFmpegKit.runCommandFlow(command).collect { state ->
                when (state) {
                    is FFmpegState.OnStart -> {
                        println("开始提取音频...")
                    }
                    is FFmpegState.OnProgress -> {
                        // state.progress 包含当前的进度时长或百分比等信息
                        println("提取中... 进度: ${state.progress}")
                    }
                    is FFmpegState.OnFinish -> {
                        println("音频提取成功！保存在: $outputMp3Path")
                    }
                    is FFmpegState.OnError -> {
                        println("提取失败: ${state.message}")
                    }
                    is FFmpegState.OnCancel -> {
                        println("任务已被取消")
                    }
                }
            }
        }
    }
}
```

---

## 4. WebRTC NS: 实时音频降噪

当你需要对麦克风采集的嘈杂声音进行降噪时，可以使用提取自 WebRTC 的降噪模块。

### 4.1 麦克风流实时降噪

```kotlin
import com.shetj.webrtc.ns.WebRtcNs

class NoiseSuppressorHelper(private val sampleRate: Int = 16000, private val channels: Int = 1) {
    private var nsHandler: Long = 0
    private var abHandler: Long = 0
    private var scHandler: Long = 0

    init {
        // 1. 初始化降噪器，level=2 (k18dB，强力降噪)
        nsHandler = WebRtcNs.webRtcNsCreate(sampleRate, channels, 2)
        // 2. 初始化必要的辅助流配置
        abHandler = WebRtcNs.createAudioBuffer(sampleRate, channels)
        scHandler = WebRtcNs.createStreamConfig(sampleRate, channels)
    }

    /**
     * 对音频数据进行原地降噪处理
     * @param audioData 录音机采集到的 PCM ShortArray 数据
     */
    fun process(audioData: ShortArray) {
        // 3. 处理数据 (方法内部会直接修改 audioData 数组的内容)
        WebRtcNs.noiseSuppressionByShort(nsHandler, abHandler, scHandler, audioData)
    }

    fun release() {
        // 4. 释放 C++ 内存
        if (nsHandler != 0L) {
            WebRtcNs.webRtcNsFree(nsHandler, abHandler, scHandler)
            nsHandler = 0L
        }
    }
}
```

---

## 5. CUrlKit & Utils: 原生网络请求与安全校验

### 5.1 发起绕过 Java 层的 HTTPS 请求并进行安全自检

```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.shetj.sdk.curl.CurlHttp
import me.shetj.sdk.utils.Utils
import android.content.Context

suspend fun secureFetchData(context: Context): String? {
    return withContext(Dispatchers.IO) {
        // 1. 执行严格的原生安全校验（包名、签名、防调试、防篡改）
        if (!Utils.strictSecurityCheck(context)) {
            println("安全校验失败！应用可能被篡改或正处于调试环境中。")
            return@withContext null
        }

        // 2. 使用 cURL 发起原生 GET 请求
        val url = "https://api.github.com/users/octocat"
        try {
            // CurlHttp.get 会在 C++ 层直接调用 libcurl，不经过系统的 HttpURLConnection 或 OkHttp
            val response = CurlHttp.get(url)
            println("获取到的数据: $response")
            return@withContext response
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
}
```

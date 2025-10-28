# SoundTouch 音频处理库

SoundTouch 是一个开源的音频处理库，专门用于音频的变速、变调处理。本项目提供了 SoundTouch C++ 库的 Android JNI 封装，支持实时音频处理和文件批处理。

## 主要功能

- **音频变速**：改变播放速度而不影响音调
- **音频变调**：改变音调而不影响播放速度  
- **组合处理**：同时调整速度和音调
- **实时处理**：支持音频流的实时处理
- **文件处理**：支持 WAV 文件的批量处理

## 相关链接

- 官网：http://www.surina.net/soundtouch
- 源码：https://codeberg.org/soundtouch/soundtouch
- 版本：2.3.1

## API 详细说明

### 实例管理

#### `newInstance(): Long`
创建新的 SoundTouch 实例
```kotlin
val soundTouch = SoundTouch()
val handle = soundTouch.newInstance()
```

#### `deleteInstance(handle: Long)`
删除实例并释放内存资源
```kotlin
soundTouch.deleteInstance(handle)
```

#### `getVersionString(): String`
获取库版本信息
```kotlin
val version = soundTouch.getVersionString() // "2.3.1"
```

#### `getErrorString(): String`
获取最后一次操作的错误信息
```kotlin
val error = soundTouch.getErrorString()
```

### 初始化设置

#### `init(handle: Long, channels: Int, sampleRate: Int, tempo: Float, pitch: Float, speed: Float)`
基础初始化方法
```kotlin
// 基础初始化：立体声，44.1kHz，正常速度，不变调
soundTouch.init(handle, 2, 44100, 1.0f, 0.0f, 1.0f)
```

#### `initExtended(handle: Long, channels: Int, sampleRate: Int, tempo: Float, pitchSemiTones: Float, pitchOctaves: Float, speed: Float)`
扩展初始化方法，支持更多音调参数
```kotlin
// 扩展初始化：支持半音和八度调整
soundTouch.initExtended(
    handle = handle,
    channels = 2,
    sampleRate = 44100,
    tempo = 1.0f,
    pitchSemiTones = 4.0f,  // 升高4个半音
    pitchOctaves = 0.0f,    // 不调整八度
    speed = 1.0f
)
```

### 速度控制

#### `setRate(handle: Long, speed: Float)`
设置播放速率倍率（同时影响速度和音调）
```kotlin
soundTouch.setRate(handle, 1.5f) // 1.5倍速播放
```

#### `setRateChange(handle: Long, rateChange: Float)`
按百分比调整播放速率，范围 [-50, 100]
```kotlin
soundTouch.setRateChange(handle, 50.0f)  // 提高50%速率
soundTouch.setRateChange(handle, -25.0f) // 降低25%速率
```

#### `setTempo(handle: Long, tempo: Float)`
设置播放节拍倍率（不影响音调）
```kotlin
soundTouch.setTempo(handle, 1.2f) // 1.2倍速播放，音调不变
```

#### `setTempoChange(handle: Long, tempoChange: Float)`
按百分比调整播放节拍，范围 [-50, 100]
```kotlin
soundTouch.setTempoChange(handle, 20.0f) // 提高20%播放速度
```

### 音调控制

#### `setPitchSemiTones(handle: Long, pitch: Float)`
使用半音调整音调，范围 [-12.0, 12.0]
```kotlin
soundTouch.setPitchSemiTones(handle, 4.0f)  // 升高4个半音
soundTouch.setPitchSemiTones(handle, -3.0f) // 降低3个半音
```

#### `setPitchOctaves(handle: Long, octaves: Float)`
使用八度调整音调，范围 [-2.0, 2.0]
```kotlin
soundTouch.setPitchOctaves(handle, 1.0f)  // 升高1个八度
soundTouch.setPitchOctaves(handle, -0.5f) // 降低半个八度
```

#### `setPitch(handle: Long, pitch: Float)`
直接设置音调倍率
```kotlin
soundTouch.setPitch(handle, 1.2f) // 音调提高20%
```

### 音频处理

#### `processFile(handle: Long, inputFile: String, outputFile: String): Int`
处理 WAV 文件
```kotlin
val result = soundTouch.processFile(handle, "/path/input.wav", "/path/output.wav")
if (result == 0) {
    println("文件处理成功")
}
```

#### `putSamples(handle: Long, samples: ShortArray, len: Int)`
输入音频采样数据（用于实时处理）
```kotlin
val audioData = ShortArray(1024)
// ... 填充音频数据
soundTouch.putSamples(handle, audioData, audioData.size)
```

#### `receiveSamples(handle: Long, outputBuf: ShortArray): Int`
接收处理后的音频数据
```kotlin
val outputBuffer = ShortArray(1024)
val samplesReceived = soundTouch.receiveSamples(handle, outputBuffer)
```

#### `flush(handle: Long, mp3buf: ShortArray): Int`
刷新处理管道，获取剩余数据
```kotlin
val finalBuffer = ShortArray(1024)
val result = soundTouch.flush(handle, finalBuffer)
```

## 典型使用案例

### 男声变声效果
```kotlin
// 方法1：使用便捷方法
soundTouch.applyMaleVoiceEffect(handle, intensity = 2) // 强度1-3

// 方法2：手动设置参数
soundTouch.setPitchSemiTones(handle, -4.0f) // 降低4个半音
```

### 女声变声效果
```kotlin
// 方法1：使用便捷方法
soundTouch.applyFemaleVoiceEffect(handle, intensity = 2) // 强度1-3

// 方法2：手动设置参数
soundTouch.setPitchSemiTones(handle, 5.0f) // 升高5个半音
```

### 机器人声音效果
```kotlin
// 使用便捷方法
soundTouch.applyRobotVoiceEffect(handle)

// 手动设置
soundTouch.setPitchSemiTones(handle, -8.0f)
soundTouch.setTempo(handle, 0.9f)
```

### 儿童声音效果
```kotlin
// 使用便捷方法
soundTouch.applyChildVoiceEffect(handle)

// 手动设置
soundTouch.setPitchSemiTones(handle, 6.0f)
soundTouch.setTempo(handle, 1.1f)
```

### 实时音频流处理示例
```kotlin
class AudioProcessor {
    private val soundTouch = SoundTouch()
    private var handle: Long = 0
    
    fun startProcessing() {
        handle = soundTouch.newInstance()
        soundTouch.init(handle, 2, 44100, 1.0f, 0.0f, 1.0f)
        
        // 设置变声效果
        soundTouch.setPitchSemiTones(handle, 4.0f) // 女声效果
        
        // 开始处理音频流
        processAudioStream()
    }
    
    private fun processAudioStream() {
        val inputBuffer = ShortArray(1024)
        val outputBuffer = ShortArray(1024)
        
        while (isProcessing) {
            // 读取音频数据到 inputBuffer
            val samplesRead = readAudioData(inputBuffer)
            
            if (samplesRead > 0) {
                // 输入数据到 SoundTouch
                soundTouch.putSamples(handle, inputBuffer, samplesRead)
                
                // 接收处理后的数据
                var samplesReceived: Int
                do {
                    samplesReceived = soundTouch.receiveSamples(handle, outputBuffer)
                    if (samplesReceived > 0) {
                        // 播放或保存处理后的音频数据
                        playAudioData(outputBuffer, samplesReceived)
                    }
                } while (samplesReceived > 0)
            }
        }
        
        // 处理完成后刷新管道
        val finalBuffer = ShortArray(1024)
        soundTouch.flush(handle, finalBuffer)
    }
    
    fun stopProcessing() {
        soundTouch.deleteInstance(handle)
    }
}
```

## 最佳实践

### 参数组合推荐

#### 自然变声效果
```kotlin
// 轻微男声效果
soundTouch.setPitchSemiTones(handle, -2.0f)

// 轻微女声效果  
soundTouch.setPitchSemiTones(handle, 3.0f)

// 明显变声但保持自然
soundTouch.setPitchSemiTones(handle, -4.0f) // 男声
soundTouch.setPitchSemiTones(handle, 5.0f)  // 女声
```

#### 特殊音效
```kotlin
// 深沉男声
soundTouch.setPitchSemiTones(handle, -6.0f)
soundTouch.setTempo(handle, 0.95f)

// 尖锐女声
soundTouch.setPitchSemiTones(handle, 7.0f)
soundTouch.setTempo(handle, 1.05f)

// 怪物声音
soundTouch.setPitchSemiTones(handle, -10.0f)
soundTouch.setTempo(handle, 0.8f)
```

### 性能优化建议

1. **缓冲区大小优化**
   ```kotlin
   // 推荐缓冲区大小：1024-4096 采样点
   val bufferSize = 2048
   val inputBuffer = ShortArray(bufferSize)
   val outputBuffer = ShortArray(bufferSize)
   ```

2. **实例复用**
   ```kotlin
   // 避免频繁创建和销毁实例
   class SoundTouchManager {
       private var handle: Long = 0
       
       fun initialize() {
           if (handle == 0L) {
               handle = soundTouch.newInstance()
           }
       }
       
       fun release() {
           if (handle != 0L) {
               soundTouch.deleteInstance(handle)
               handle = 0L
           }
       }
   }
   ```

3. **参数预设**
   ```kotlin
   // 预定义常用效果，避免重复计算
   object VoiceEffects {
       const val MALE_LIGHT = -2.0f
       const val MALE_NORMAL = -4.0f
       const val MALE_HEAVY = -6.0f
       const val FEMALE_LIGHT = 3.0f
       const val FEMALE_NORMAL = 5.0f
       const val FEMALE_HEAVY = 7.0f
   }
   ```

### 常见问题解决方案

#### 1. 音质失真问题
**问题**：参数设置过大导致音质严重失真
```kotlin
// ❌ 错误：参数过大
soundTouch.setPitchSemiTones(handle, 15.0f) // 超出推荐范围

// ✅ 正确：使用合理范围
soundTouch.setPitchSemiTones(handle, 8.0f)  // 在推荐范围内
```

#### 2. 处理延迟问题
**问题**：实时处理时出现明显延迟
```kotlin
// ✅ 解决方案：使用较小的缓冲区
val bufferSize = 1024 // 而不是 8192
```

#### 3. 内存泄漏问题
**问题**：忘记释放 SoundTouch 实例
```kotlin
// ✅ 正确的资源管理
class AudioProcessor : AutoCloseable {
    private var handle: Long = 0
    
    init {
        handle = soundTouch.newInstance()
    }
    
    override fun close() {
        if (handle != 0L) {
            soundTouch.deleteInstance(handle)
            handle = 0L
        }
    }
}

// 使用 try-with-resources
AudioProcessor().use { processor ->
    // 处理音频
}
```

#### 4. 参数验证错误
**问题**：传入无效参数导致崩溃
```kotlin
// ✅ 添加参数验证
fun setSafePitchSemiTones(handle: Long, pitch: Float) {
    val clampedPitch = pitch.coerceIn(-12.0f, 12.0f)
    soundTouch.setPitchSemiTones(handle, clampedPitch)
}
```

#### 5. 音频格式不匹配
**问题**：输入的音频格式与初始化参数不匹配
```kotlin
// ✅ 确保参数一致
val channels = 2      // 立体声
val sampleRate = 44100 // 44.1kHz

soundTouch.init(handle, channels, sampleRate, 1.0f, 0.0f, 1.0f)

// 确保输入的音频数据也是相同格式
// channels = 2, sampleRate = 44100
```

## 注意事项

1. **参数范围**：严格遵守各参数的有效范围，超出范围可能导致音质失真或程序崩溃
2. **资源管理**：及时释放 SoundTouch 实例，避免内存泄漏
3. **线程安全**：SoundTouch 不是线程安全的，多线程使用时需要额外同步
4. **音频格式**：确保输入音频格式与初始化参数一致
5. **性能考虑**：实时处理时注意缓冲区大小和处理频率的平衡
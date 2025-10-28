package me.shetj.ndk.soundtouch

/**
 * SoundTouch音频处理库的Kotlin封装类
 * 
 * SoundTouch是一个开源的音频处理库，主要用于音频的变速、变调处理。
 * 本类提供了对SoundTouch C++库的JNI封装，支持实时音频处理和文件处理。
 * 
 * 主要功能：
 * - 音频变速（不改变音调）
 * - 音频变调（不改变播放速度）
 * - 音频变速变调组合处理
 * - 实时音频流处理
 * - 音频文件批处理
 * 
 * @author stj
 * @Date 2021/11/4-18:17
 * @Email 375105540@qq.com
 * @version 2.3.1
 */
class SoundTouch {

    companion object {
        /**
         * 静态初始化块，加载SoundTouch本地库
         * 在类首次使用时自动执行
         */
        init {
            System.loadLibrary("soundTouch")
        }
        
        // 参数范围常量
        const val MIN_CHANNELS = 1
        const val MAX_CHANNELS = 2
        const val MIN_SAMPLE_RATE = 8000
        const val MAX_SAMPLE_RATE = 192000
        const val MIN_PITCH_SEMITONES = -12.0f
        const val MAX_PITCH_SEMITONES = 12.0f
        const val MIN_PITCH_OCTAVES = -2.0f
        const val MAX_PITCH_OCTAVES = 2.0f
        const val MIN_RATE_CHANGE = -50.0f
        const val MAX_RATE_CHANGE = 100.0f
        const val MIN_TEMPO_CHANGE = -50.0f
        const val MAX_TEMPO_CHANGE = 100.0f
    }

    /**
     * 创建新的SoundTouch实例
     * 
     * @return SoundTouch实例的句柄，用于后续所有操作
     * @throws RuntimeException 如果创建实例失败
     */
    external fun newInstance(): Long

    /**
     * 删除SoundTouch实例，释放内存资源
     * 
     * @param handle SoundTouch实例句柄
     * @throws IllegalArgumentException 如果句柄无效
     */
    external fun deleteInstance(handle: Long)

    /**
     * 获取SoundTouch库版本信息
     * 
     * @return 版本字符串，格式如"2.3.1"
     */
    external fun getVersionString(): String

    /**
     * 获取最后一次操作的错误信息
     * 
     * @return 错误信息字符串，如果没有错误则返回空字符串
     */
    external fun getErrorString(): String

    /**
     * 初始化SoundTouch实例的基本参数
     * 
     * 此方法设置音频处理的基础参数，包括声道数、采样率和各种音效参数。
     * 必须在进行任何音频处理之前调用此方法。
     * 
     * @param handle SoundTouch实例句柄
     * @param channels 声道数：1=单声道，2=立体声
     * @param sampleRate 采样率，常用值：8000, 16000, 22050, 44100, 48000 Hz
     * @param tempo 播放速度倍率，1.0=正常速度，<1.0变慢，>1.0变快
     * @param pitch 音调调整（半音为单位），0=不变，正值升调，负值降调
     * @param speed 播放速率，1.0=正常速率，<1.0变慢，>1.0变快
     * 
     * @throws IllegalArgumentException 如果参数超出有效范围
     * @throws RuntimeException 如果初始化失败
     */
    external fun init(
        handle: Long,
        channels: Int,
        sampleRate: Int,
        tempo: Float,
        pitch: Float,
        speed: Float
    )

    /**
     * 扩展的初始化方法，支持更多音调参数
     * 
     * @param handle SoundTouch实例句柄
     * @param channels 声道数：1=单声道，2=立体声
     * @param sampleRate 采样率
     * @param tempo 播放速度倍率
     * @param pitchSemiTones 半音调整值，范围[-12.0, 12.0]
     * @param pitchOctaves 八度调整值，范围[-2.0, 2.0]
     * @param speed 播放速率
     * 
     * @throws IllegalArgumentException 如果参数超出有效范围
     */
    fun initExtended(
        handle: Long,
        channels: Int,
        sampleRate: Int,
        tempo: Float,
        pitchSemiTones: Float = 0.0f,
        pitchOctaves: Float = 0.0f,
        speed: Float = 1.0f
    ) {
        validateParameters(channels, sampleRate, pitchSemiTones, pitchOctaves)
        init(handle, channels, sampleRate, tempo, pitchSemiTones, speed)
        if (pitchOctaves != 0.0f) {
            setPitchOctaves(handle, pitchOctaves)
        }
    }

    /**
     * 设置播放速率变化百分比
     * 
     * 在原始速率1.0的基础上按百分比调整播放速率。
     * 此方法不会改变音调，只改变播放速度。
     * 
     * @param handle SoundTouch实例句柄
     * @param rateChange 速率变化百分比，范围[-50, 100]
     *                   -50% = 0.5倍速，0% = 正常速度，100% = 2倍速
     * 
     * @throws IllegalArgumentException 如果rateChange超出范围[-50, 100]
     */
    external fun setRateChange(handle: Long, rateChange: Float)

    /**
     * 设置播放节拍变化百分比
     * 
     * 在原始节拍1.0的基础上按百分比调整播放节拍。
     * 此方法通过时间拉伸改变播放速度，不影响音调。
     * 
     * @param handle SoundTouch实例句柄
     * @param tempoChange 节拍变化百分比，范围[-50, 100]
     *                    负值变慢，正值变快
     * 
     * @throws IllegalArgumentException 如果tempoChange超出范围[-50, 100]
     */
    external fun setTempoChange(handle: Long, tempoChange: Float)

    /**
     * 设置播放节拍倍率
     * 
     * 直接设置播放节拍的倍率值。
     * 1.0 = 正常速度，<1.0 = 变慢，>1.0 = 变快
     * 
     * @param handle SoundTouch实例句柄
     * @param tempo 节拍倍率，建议范围[0.5, 2.0]
     */
    external fun setTempo(handle: Long, tempo: Float)

    /**
     * 设置音调调整（以半音为单位）
     * 
     * 使用半音阶调整音调，不改变播放速度。
     * 半音是音乐理论中最小的音程单位。
     * 
     * @param handle SoundTouch实例句柄
     * @param pitch 半音调整值，范围[-12.0, 12.0]
     *              正值升高音调，负值降低音调
     *              12个半音 = 1个八度
     * 
     * 常用参考值：
     * - 男声变女声：+4 到 +8
     * - 女声变男声：-4 到 -8
     * - 机器人效果：±10 到 ±12
     * 
     * @throws IllegalArgumentException 如果pitch超出范围[-12.0, 12.0]
     */
    external fun setPitchSemiTones(handle: Long, pitch: Float)
    
    /**
     * 设置音高调整（以八度为单位）
     * 
     * 使用八度调整音调，八度是音乐中的基本音程单位。
     * 1个八度 = 12个半音，频率关系为2:1。
     * 
     * @param handle SoundTouch实例句柄
     * @param octaves 八度调整值，范围[-2.0, 2.0]
     *                正值提高音高，负值降低音高
     *                1.0 = 提高一个八度（频率翻倍）
     *                -1.0 = 降低一个八度（频率减半）
     * 
     * @throws IllegalArgumentException 如果octaves超出范围[-2.0, 2.0]
     */
    external fun setPitchOctaves(handle: Long, octaves: Float)

    /**
     * 设置直接音调倍率
     * 
     * 直接设置音调的倍率值，1.0为原始音调。
     * 
     * @param handle SoundTouch实例句柄
     * @param pitch 音调倍率，1.0=原始音调，<1.0降调，>1.0升调
     */
    external fun setPitch(handle: Long, pitch: Float)

    /**
     * 设置播放速率倍率
     * 
     * 直接设置播放速率，会同时影响播放速度和音调。
     * 
     * @param handle SoundTouch实例句柄
     * @param speed 速率倍率，1.0=正常速率，<1.0变慢，>1.0变快
     */
    external fun setRate(handle: Long, speed: Float)

    /**
     * 处理音频文件
     * 
     * 直接处理WAV格式的音频文件，应用当前设置的所有音效参数。
     * 此方法适用于批量处理，不适合实时处理。
     * 
     * @param handle SoundTouch实例句柄
     * @param inputFile 输入文件路径，必须是WAV格式
     * @param outputFile 输出文件路径，将生成WAV格式文件
     * 
     * @return 处理结果：0=成功，非0=失败
     * @throws IllegalArgumentException 如果文件路径无效
     * @throws RuntimeException 如果处理过程中发生错误
     */
    external fun processFile(handle: Long, inputFile: String, outputFile: String): Int

    /**
     * 输入音频采样数据
     * 
     * 将音频采样数据输入到SoundTouch处理管道中。
     * 此方法用于实时音频流处理，需要与receiveSamples()配合使用。
     * 
     * 注意：putSamples的调用次数可能小于receiveSamples的调用次数，
     * 因为SoundTouch内部有缓冲机制。
     * 
     * @param handle SoundTouch实例句柄
     * @param samples 音频采样数据数组（16位PCM格式）
     * @param len 有效采样数据长度（以采样点为单位，不是字节数）
     * 
     * @throws IllegalArgumentException 如果samples为空或len无效
     * @throws RuntimeException 如果处理失败
     */
    external fun putSamples(handle: Long, samples: ShortArray, len: Int)

    /**
     * 接收处理后的音频数据
     * 
     * 从SoundTouch处理管道中获取处理后的音频数据。
     * 此方法需要循环调用，直到返回0表示没有更多数据。
     * 
     * @param handle SoundTouch实例句柄
     * @param outputBuf 输出缓冲区，用于接收处理后的音频数据
     * 
     * @return 实际接收到的采样数据长度（以采样点为单位）
     *         0表示当前没有可用数据
     * 
     * @throws IllegalArgumentException 如果outputBuf为空
     * @throws RuntimeException 如果接收失败
     */
    external fun receiveSamples(handle: Long, outputBuf: ShortArray): Int

    /**
     * 刷新处理管道
     * 
     * 强制输出处理管道中剩余的音频数据。
     * 在音频流处理结束时调用，确保所有数据都被处理完毕。
     * 
     * @param handle SoundTouch实例句柄
     * @param mp3buf 输出缓冲区，接收最后的音频数据
     * 
     * @return 刷新结果：0=成功，非0=失败
     * @throws RuntimeException 如果刷新失败
     */
    external fun flush(handle: Long, mp3buf: ShortArray): Int

    /**
     * 参数有效性校验
     * 
     * @param channels 声道数
     * @param sampleRate 采样率
     * @param pitchSemiTones 半音调整值
     * @param pitchOctaves 八度调整值
     * 
     * @throws IllegalArgumentException 如果任何参数超出有效范围
     */
    private fun validateParameters(
        channels: Int,
        sampleRate: Int,
        pitchSemiTones: Float = 0.0f,
        pitchOctaves: Float = 0.0f
    ) {
        if (channels < MIN_CHANNELS || channels > MAX_CHANNELS) {
            throw IllegalArgumentException("声道数必须在 $MIN_CHANNELS 到 $MAX_CHANNELS 之间")
        }
        if (sampleRate < MIN_SAMPLE_RATE || sampleRate > MAX_SAMPLE_RATE) {
            throw IllegalArgumentException("采样率必须在 $MIN_SAMPLE_RATE 到 $MAX_SAMPLE_RATE 之间")
        }
        if (pitchSemiTones < MIN_PITCH_SEMITONES || pitchSemiTones > MAX_PITCH_SEMITONES) {
            throw IllegalArgumentException("半音调整值必须在 $MIN_PITCH_SEMITONES 到 $MAX_PITCH_SEMITONES 之间")
        }
        if (pitchOctaves < MIN_PITCH_OCTAVES || pitchOctaves > MAX_PITCH_OCTAVES) {
            throw IllegalArgumentException("八度调整值必须在 $MIN_PITCH_OCTAVES 到 $MAX_PITCH_OCTAVES 之间")
        }
    }

    /**
     * 便捷方法：男声变声效果
     * 
     * @param handle SoundTouch实例句柄
     * @param intensity 变声强度，范围[1, 3]，1=轻微，3=明显
     */
    fun applyMaleVoiceEffect(handle: Long, intensity: Int = 2) {
        val pitchAdjustment = when (intensity) {
            1 -> -2.0f
            2 -> -4.0f
            3 -> -6.0f
            else -> -4.0f
        }
        setPitchSemiTones(handle, pitchAdjustment)
    }

    /**
     * 便捷方法：女声变声效果
     * 
     * @param handle SoundTouch实例句柄
     * @param intensity 变声强度，范围[1, 3]，1=轻微，3=明显
     */
    fun applyFemaleVoiceEffect(handle: Long, intensity: Int = 2) {
        val pitchAdjustment = when (intensity) {
            1 -> 3.0f
            2 -> 5.0f
            3 -> 7.0f
            else -> 5.0f
        }
        setPitchSemiTones(handle, pitchAdjustment)
    }

    /**
     * 便捷方法：机器人声音效果
     * 
     * @param handle SoundTouch实例句柄
     */
    fun applyRobotVoiceEffect(handle: Long) {
        setPitchSemiTones(handle, -8.0f)
        setTempo(handle, 0.9f)
    }

    /**
     * 便捷方法：儿童声音效果
     * 
     * @param handle SoundTouch实例句柄
     */
    fun applyChildVoiceEffect(handle: Long) {
        setPitchSemiTones(handle, 6.0f)
        setTempo(handle, 1.1f)
    }
}
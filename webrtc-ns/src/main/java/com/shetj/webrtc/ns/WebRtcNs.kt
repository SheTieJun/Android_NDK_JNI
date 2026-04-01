package com.shetj.webrtc.ns

object WebRtcNs {


    init {
        System.loadLibrary("webrtc-ns")
    }

    /**
     * 创建
     * @param sampleRate 采样率
     * @param num_channels 声道数
     * @param level 降噪强度
     *
     * 0 = SuppressionLevel::k6dB
     * 1 = SuppressionLevel::k12dB
     * 2 =  SuppressionLevel::k18dB
     * 3 = SuppressionLevel::k21dB
     * @return
     */
    external fun webRtcNsCreate(sampleRate: Int, num_channels: Int, level: Int): Long


    external fun createAudioBuffer(sampleRate: Int, num_channels: Int):Long

    external fun createStreamConfig(sampleRate: Int, num_channels: Int):Long

    /**
     * 释放
     * @param nsHandler
     */
    external fun webRtcNsFree(nsHandler: Long,abHandler:Long,scHandler:Long)

    /**
     * 音频降噪 (处理字节数据)
     * 
     * ⚠️ 注意：WebRTC 降噪算法内部严格以 10ms 为一帧进行处理。
     * 例如 16000Hz 采样率下，10ms 包含 160 个样本（320字节）。
     * 传入的数组长度最好是 10ms 的整数倍，否则尾部不足 10ms 的数据将被原样保留（不处理）。
     */
    external fun noiseSuppressionByBytes(
        nsHandler: Long,
        abHandler:Long,scHandler:Long,
        inputbuffer: ByteArray
    )


    /**
     * 音频降噪 (处理 Short 数据)
     * 
     * ⚠️ 注意：WebRTC 降噪算法内部严格以 10ms 为一帧进行处理。
     * 例如 44100Hz 采样率下，10ms 包含 441 个样本。
     * 传入的数组长度最好是 10ms 的整数倍，否则尾部不足 10ms 的数据将被原样保留（不处理）。
     */
    external fun noiseSuppressionByShort(
        nsHandler: Long,abHandler:Long,scHandler:Long,
        inputbuffer: ShortArray,
    )
}
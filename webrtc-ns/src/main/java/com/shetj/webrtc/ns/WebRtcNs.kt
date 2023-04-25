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
     * 音频降噪
     */
    external fun noiseSuppressionByBytes(
        nsHandler: Long,
        abHandler:Long,scHandler:Long,
        inputbuffer: ByteArray
    )


    /**
     * 音频降噪
     */
    external fun noiseSuppressionByShort(
        nsHandler: Long,abHandler:Long,scHandler:Long,
        inputbuffer: ShortArray,
    )
}
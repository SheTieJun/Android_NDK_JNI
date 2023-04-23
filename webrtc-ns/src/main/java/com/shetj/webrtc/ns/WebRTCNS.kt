package com.shetj.webrtc.ns

class WebRTCNS {


     companion object {
          init {
                System.loadLibrary("webrtc_ns")
          }
     }

    /**
     * 创建
     * @param sampleRate 采样率
     * @param num_channels 声道数
     * @return
     */
    external fun WebRtcNsCreate(sampleRate:Int, num_channels:Int): Long

    /**
     * 释放
     * @param nsHandler
     */
    external fun WebRtcNsFree(nsHandler: Long)


    /**
     * 音频降噪
     * @param file_in 输入文件
     * @param file_out 输出文件
     * @param sample 采样率
     * @param mode 降噪强度0-2
     */
    external fun noiseSuppression(nsHandler: Long,file_in: String, file_out: String, sample: Int, mode: Int)

    /**
     * 音频降噪
     * @param buffer 音频文件流
     * @param sample 同上
     * @param mode   同上
     */
    external fun noiseSuppressionByBytes(inputbuffer: ByteArray, sample: Int, mode: Int)

}
package me.shetj.sdk.ffmepg.demo

import android.util.Log
import com.shetj.webrtc.ns.WebRtcNs

/**
 * 为了支持边录边处理且解决 10ms 帧对齐问题，将 WebRtcNsKit 改为实例类，
 * 每个录音器/通道可以维护自己的处理上下文和未处理的残留数据缓冲区（Residual Buffer）。
 */
class WebRtcNsKit {

    private var nsHandler: Long = -1
    private var abHandler: Long = -1
    private var scHandler: Long = -1
    
    // 缓存上一次不足 10ms 的残留数据
    private var residualBuffer = ShortArray(0)
    // 每次处理所需的样本数 (对应 10ms)
    private var samplesPer10ms: Int = 0

    fun create(sampleRate: Int = 48000, num_channels: Int = 1, level: Int = 1) {
        nsHandler = WebRtcNs.webRtcNsCreate(sampleRate, num_channels, level)
        abHandler = WebRtcNs.createAudioBuffer(sampleRate, num_channels)
        scHandler = WebRtcNs.createStreamConfig(sampleRate, num_channels)
        
        // 计算 10ms 对应的样本数：采样率 * 声道数 * 10 / 1000
        samplesPer10ms = sampleRate * num_channels / 100
        residualBuffer = ShortArray(0)
    }

    fun free() {
        if (nsHandler == -1L) return
        WebRtcNs.webRtcNsFree(nsHandler, abHandler, scHandler)
        nsHandler = -1L
        abHandler = -1L
        scHandler = -1L
        residualBuffer = ShortArray(0)
    }

    /**
     * 智能降噪处理，自带残留数据拼接。
     * 由于底层算法严格要求 10ms 处理，将不足 10ms 的数据存入 residualBuffer，
     * 下次调用时先拼接残留数据再凑齐 10ms 送给底层降噪。
     * 
     * @param inputbuffer 本次需要降噪的原始数据
     * @return 降噪后的数据（长度可能因为拼凑而与输入不完全相等，属于正常现象）
     */
    fun processAndFlush(inputbuffer: ShortArray): ShortArray {
        if (nsHandler == -1L) {
            Log.e("WebRtcNsKit", "nsHandler == -1L, please call create() before")
            return inputbuffer
        }

        // 1. 将上次残留的数据和本次的新数据拼接起来
        val combinedLength = residualBuffer.size + inputbuffer.size
        val combinedBuffer = ShortArray(combinedLength)
        if (residualBuffer.isNotEmpty()) {
            System.arraycopy(residualBuffer, 0, combinedBuffer, 0, residualBuffer.size)
        }
        System.arraycopy(inputbuffer, 0, combinedBuffer, residualBuffer.size, inputbuffer.size)

        // 2. 计算可以凑出多少个完整的 10ms 帧
        val validLength = combinedLength - (combinedLength % samplesPer10ms)
        
        // 3. 截取有效部分去降噪
        val processBuffer = ShortArray(validLength)
        System.arraycopy(combinedBuffer, 0, processBuffer, 0, validLength)
        
        if (validLength > 0) {
            WebRtcNs.noiseSuppressionByShort(nsHandler, abHandler, scHandler, processBuffer)
        }

        // 4. 将剩下的不足 10ms 的数据存入缓存，留到下次拼接
        val leftoverLength = combinedLength - validLength
        residualBuffer = ShortArray(leftoverLength)
        if (leftoverLength > 0) {
            System.arraycopy(combinedBuffer, validLength, residualBuffer, 0, leftoverLength)
        }

        return processBuffer
    }

    @Deprecated("建议使用 processAndFlush 来处理不规则长度的数组")
    fun noiseSuppressionByShort(inputbuffer: ShortArray) {
        if (nsHandler == -1L) {
            Log.e("WebRtcNsKit", "nsHandler == -1L,please call create() before")
            return
        }
        WebRtcNs.noiseSuppressionByShort(nsHandler, abHandler, scHandler, inputbuffer)
    }
}
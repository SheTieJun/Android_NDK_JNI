package me.shetj.sdk.ffmepg.demo

import com.shetj.webrtc.ns.WebRtcNs

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2023/4/24<br>
 */
object WebRtcNsKit {

    private var nsHandler: Long = -1
    private var abHandler:Long  = -1
    private var scHandler:Long = -1

    fun create(sampleRate: Int = 48000, num_channels: Int = 1, level: Int = 1) {
        nsHandler = WebRtcNs.webRtcNsCreate(sampleRate, num_channels, level)
        abHandler = WebRtcNs.createAudioBuffer(sampleRate, num_channels)
        scHandler = WebRtcNs.createStreamConfig(sampleRate, num_channels)
    }


    fun free() {
        if (nsHandler == -1L) return
        WebRtcNs.webRtcNsFree(nsHandler,abHandler,scHandler)
    }


    fun noiseSuppressionByBytes(inputbuffer: ByteArray) {
        if (nsHandler == -1L) return
        WebRtcNs.noiseSuppressionByBytes(nsHandler,abHandler,scHandler, inputbuffer)
    }

    fun noiseSuppressionByShort(inputbuffer: ShortArray) {
        if (nsHandler == -1L) return
        WebRtcNs.noiseSuppressionByShort(nsHandler,abHandler,scHandler, inputbuffer)
    }


}
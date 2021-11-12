package me.shetj.sdk.ffmepg.demo

import android.util.Log
import me.shetj.ndk.soundtouch.SoundTouch

/**
 * @author stj
 * @Date 2021/11/4-18:17
 * @Email 375105540@qq.com
 */

class STKit : ISoundTouch {

    companion object {


        @Volatile
        private var sInstance: STKit? = null

        fun getInstance(): STKit {
            return sInstance ?: synchronized(STKit::class.java) {
                return STKit()
            }
        }

        fun onDestroy() {
            sInstance?.close()
            sInstance = null
        }

    }

    private val soundTouch: SoundTouch by lazy { SoundTouch() }


    private var handle: Long = 0


    override fun init(channels: Int, sampleRate: Int, tempo: Float, pitch: Float, rate: Float) {
        if (handle == 0L) {
            handle = soundTouch.newInstance()
        }
        Log.e(
            "soundTouch",
            "version:${soundTouch.getVersionString()}handle:${handle},init:channels: $channels, sampleRate: $sampleRate, tempo: $tempo, " +
                    "pitch: $pitch, rate: $rate"
        )
        soundTouch.init(handle,channels, sampleRate, tempo, pitch, rate)
    }

    override fun setRateChange(rateChange: Float) {
        soundTouch.setRateChange(handle,rateChange)
    }

    override fun setTempoChange(tempoChange: Float) {
        soundTouch.setTempoChange(handle,tempoChange)
    }


    //处理玩最后的数据
    override fun flush(mp3buf: ShortArray): Int {
        return soundTouch.flush(handle,mp3buf)
    }

    override fun close() {
        if (handle != 0L) {
            soundTouch.deleteInstance(handle)
            handle = 0
        }
    }

    //  指定节拍，设置新的节拍tempo，源tempo=1.0，小于1则变慢；大于1变快
    override fun setTempo(tempo: Float) {
        soundTouch.setTempo(handle,tempo)
    }

    //在源pitch的基础上，使用半音(Semitones)设置新的pitch [-12.0,12.0]
    override fun setPitchSemiTones(pitch: Float) {
        soundTouch.setPitchSemiTones(handle,pitch)
    }

    //指定播放速率
    override fun setRate(speed: Float) {
        soundTouch.setRate(handle,speed)
    }

    override fun processFile(inputFile: String, outputFile: String): Boolean {
        return if (soundTouch.processFile(handle,inputFile, outputFile) == 0) {
            true
        } else {
            Log.e("soundTouch", soundTouch.getErrorString())
            false
        }
    }

    override fun receiveSamples(outputBuf: ShortArray): Int {
        return soundTouch.receiveSamples(handle,outputBuf)
    }

    override fun putSamples(samples: ShortArray, len: Int) {
        soundTouch.putSamples(handle,samples, len)
    }

    fun getVersion(): String {
        return soundTouch.getVersionString()
    }
}
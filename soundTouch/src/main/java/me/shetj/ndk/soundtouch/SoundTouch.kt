package me.shetj.ndk.soundtouch

import android.util.Log

/**
 * @author stj
 * @Date 2021/11/4-18:17
 * @Email 375105540@qq.com
 */

class SoundTouch : ISoundTouch {

    companion object {

        init {
            System.loadLibrary("soundTouch")
        }

        @Volatile
        private var sInstance: ISoundTouch? = null

        fun getInstance(): ISoundTouch {
            return sInstance ?: synchronized(SoundTouch::class.java) {
                return SoundTouch()
            }
        }

        fun onDestroy() {
            sInstance?.close()
            sInstance = null
        }

    }




    private external fun newInstance(): Long
    private external fun deleteInstance(handle: Long)
    private external fun getVersionString(): String
    private external fun getErrorString(): String

    private external fun init(
        handle: Long,
        channels: Int, //设置声道(1单,2双)
        sampleRate: Int,//设置采样率
        tempo: Int, //指定节拍，设置新的节拍tempo，源tempo=1.0，小于1则变慢；大于1变快
        pitch: Float, //指定音调值
        speed: Float//指定播放速率
    )

    // 在原速1.0基础上，按百分比做增量，取值(-50 .. +100 %)
    private external fun setRateChange(handle: Long, rateChange: Float)
    private external fun setTempoChange(handle: Long, tempoChange: Float)
    private external fun setTempo(handle: Long, tempo: Float)
    private external fun setPitchSemiTones(handle: Long, pitch: Float)
    private external fun setRate(handle: Long, speed: Float)

    //直接WAV处理文件
    private external fun processFile(handle: Long, inputFile: String, outputFile: String): Int


    //实时处理PCM 流
    private external fun processSamples(
        handle: Long,
        input: ByteArray?,
        samples: Int,
        output: ByteArray?,
    ): Int

    //获取最后一段数据
    private external fun flush(handle: Long, mp3buf: ByteArray): Int

    private var handle: Long = 0


    override fun init(channels: Int, sampleRate: Int, tempo: Int, pitch: Float, speed: Float) {
        if (handle == 0L){
            handle = newInstance()
        }
        init(handle, channels, sampleRate, tempo, pitch, speed)
    }

    override fun setRateChange(rateChange: Float) {
        check(handle == 0L){
            "you should init first"
        }
        setRateChange(handle, rateChange)
    }

    override fun setTempoChange(tempoChange: Float) {
        check(handle == 0L){
            "you should init first"
        }
        setTempoChange(handle, tempoChange)
    }


    override fun processSamples(input: ByteArray?, samples: Int, output: ByteArray?) {
        check(handle == 0L){
            "you should init first"
        }
          if (processSamples(handle, input, samples, output) != 0){
              throw error(getErrorString())
          }
    }

    //处理玩最后的数据
    override fun flush(mp3buf: ByteArray): Int {
        check(handle == 0L){
            "you should init first"
        }
        return flush(handle, mp3buf)
    }

    override fun close() {
        if (handle != 0L) {
            deleteInstance(handle)
            handle = 0
        }
    }

    //  指定节拍，设置新的节拍tempo，源tempo=1.0，小于1则变慢；大于1变快
    override fun setTempo(tempo: Float) {
        check(handle == 0L){
            "you should init first"
        }
        setTempo(handle, tempo)
    }

    //在源pitch的基础上，使用半音(Semitones)设置新的pitch [-12.0,12.0]
    override fun setPitchSemiTones(pitch: Float) {
        check(handle == 0L){
            "you should init first"
        }
        setPitchSemiTones(handle, pitch)
    }

    //指定播放速率
    override fun setRate(speed: Float) {
        check(handle == 0L){
            "you should init first"
        }
        setRate(handle, speed)
    }

    override fun processFile(inputFile: String, outputFile: String): Boolean {
        check(handle == 0L){
            "you should init first"
        }
        return if ( processFile(handle, inputFile, outputFile) == 0){
            true
        }else{
            Log.e("SoundTouch",getErrorString())
            false
        }
    }

}
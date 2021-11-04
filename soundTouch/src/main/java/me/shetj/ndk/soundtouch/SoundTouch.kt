package me.shetj.ndk.soundtouch

/**
 * @author stj
 * @Date 2021/11/4-18:17
 * @Email 375105540@qq.com
 */

object SoundTouch {
    init {
        System.loadLibrary("soundTouch")
    }

    private external fun setTempo(handle: Long, tempo: Float)
    private external fun setPitchSemiTones(handle: Long, pitch: Float)
    private external fun setSpeed(handle: Long, speed: Float)
    private external fun processFile(handle: Long, inputFile: String, outputFile: String): Int
    private external fun deleteInstance(handle: Long)
    external fun newInstance(): Long
    external fun getVersionString(): String
    external fun getErrorString(): String

    var handle: Long = 0

    fun close() {
        deleteInstance(handle)
        handle = 0
    }

    fun setTempo(tempo: Float) {
        setTempo(handle, tempo)
    }

    fun setPitchSemiTones(pitch: Float) {
        setPitchSemiTones(handle, pitch)
    }

    fun setSpeed(speed: Float) {
        setSpeed(handle, speed)
    }

    fun processFile(inputFile: String, outputFile: String): Int {
        return processFile(handle, inputFile, outputFile)
    }


    init {
        handle = newInstance()
    }
}
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
    val versionString: String?
        external get
    val errorString: String?
        external get

    external fun setTempo(handle: Long, tempo: Float)
    external fun setPitchSemiTones(handle: Long, pitch: Float)
    external fun setSpeed(handle: Long, speed: Float)
    external fun processFile(handle: Long, inputFile: String, outputFile: String): Int
    external fun deleteInstance(handle: Long)
    external fun newInstance(): Long


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
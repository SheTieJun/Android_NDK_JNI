package me.shetj.ndk.soundtouch

/**
 * @author stj
 * @Date 2021/11/4-18:17
 * @Email 375105540@qq.com
 */

object SoundTouchKit {
    init {
        System.loadLibrary("soundTouch_lib")
    }



}
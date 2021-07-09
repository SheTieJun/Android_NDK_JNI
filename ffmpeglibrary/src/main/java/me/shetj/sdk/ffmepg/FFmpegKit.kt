package me.shetj.sdk.ffmepg

object FFmpegKit {
    external fun checkFFmpeg(): String?

    init {
        System.loadLibrary("ffmpeg_lib")
    }
}
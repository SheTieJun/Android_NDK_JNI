package me.shetj.sdk.ffmepg

internal object FFmpegKit {
    external fun checkFFmpeg(): String?

    init {
        System.loadLibrary("libavcodec")
        System.loadLibrary("libavfilter")
        System.loadLibrary("libavformat")
        System.loadLibrary("libavutil")
        System.loadLibrary("libswresample")
        System.loadLibrary("libswscale")
        System.loadLibrary("ffmpeg_lib")
    }
}
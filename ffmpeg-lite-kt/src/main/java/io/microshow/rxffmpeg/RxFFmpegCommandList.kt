package io.microshow.rxffmpeg

import android.util.Log
import kotlin.jvm.Synchronized
import io.microshow.rxffmpeg.player.IMediaPlayer
import io.microshow.rxffmpeg.player.MeasureHelper.VideoSizeInfo
import io.microshow.rxffmpeg.player.MeasureHelper.FitModel
import io.microshow.rxffmpeg.player.RxFFmpegPlayerView
import io.microshow.rxffmpeg.player.BaseMediaPlayer
import io.microshow.rxffmpeg.player.IMediaPlayer.OnLoadingListener
import io.microshow.rxffmpeg.player.IMediaPlayer.OnTimeUpdateListener
import kotlin.jvm.JvmOverloads
import io.microshow.rxffmpeg.player.RxFFmpegPlayer
import io.microshow.rxffmpeg.player.RxFFmpegPlayerImpl
import io.microshow.rxffmpeg.player.RxFFmpegPlayerView.PlayerCoreType
import io.microshow.rxffmpeg.player.MeasureHelper
import io.microshow.rxffmpeg.player.RxFFmpegPlayerController
import io.microshow.rxffmpeg.player.ScaleTextureView
import io.microshow.rxffmpeg.player.SystemMediaPlayerImpl
import io.microshow.rxffmpeg.player.RxFFmpegPlayerView.VideoSizeChangedListener
import io.microshow.rxffmpeg.player.SystemMediaPlayer
import io.microshow.rxffmpeg.player.RxFFmpegPlayerControllerImpl.PlayerListener
import io.microshow.rxffmpeg.player.RxFFmpegPlayerControllerImpl
import io.microshow.rxffmpeg.RxFFmpegInvoke
import kotlin.jvm.Volatile
import io.microshow.rxffmpeg.RxFFmpegInvoke.IFFmpegListener
import io.microshow.rxffmpeg.RxFFmpegCommandList
import java.lang.StringBuilder
import java.util.ArrayList

/**
 * 指令集合,
 * 默认会加上 ffmpeg -y，如果想去除默认的指令可以调用clearCommands()清除
 *
 *
 * Created by Super on 2019/4/5.
 */
internal class RxFFmpegCommandList : ArrayList<String>() {
    /**
     * 清除命令集合
     */
    fun clearCommands(): RxFFmpegCommandList {
        this.clear()
        return this
    }

    /**
     * 追加命令
     *
     * @param s cmd
     * @return RxFFmpegCommandList
     */
    fun append(s: String): RxFFmpegCommandList {
        this.add(s)
        return this
    }

    /**
     * 构建命令
     *
     * @return -
     */
    fun build(): Array<String> {
        return this.toTypedArray()
    }

    /**
     * 构建命令
     *
     * @param isLog true:构建命令后 Log打印命令日志;  false :不打印命令日志
     * @return -
     */
    fun build(isLog: Boolean): Array<String> {
        val cmds: Array<String> = build()
        if (isLog) { //需要打印构建后的命令
            val cmdLogStr: StringBuilder = StringBuilder()
            for (i in cmds.indices) {
                cmdLogStr.append(cmds[i])
                if (i < cmds.size - 1) {
                    cmdLogStr.append(" ")
                }
            }
            Log.e("TAG_FFMPEG", "cmd: $cmdLogStr")
        }
        return cmds
    }

    init {
        this.add("ffmpeg")
        this.add("-y")
    }
}
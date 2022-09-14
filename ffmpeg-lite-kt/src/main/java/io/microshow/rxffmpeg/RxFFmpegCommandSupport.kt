package io.microshow.rxffmpeg

import android.text.TextUtils
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

internal object RxFFmpegCommandSupport {
    /**
     * 给视频加毛玻璃效果
     *
     * @param inputPath  输入视频文件
     * @param outputPath 输出视频文件
     * @param boxblur    blur效果调节，默认 "5:1"
     * @param isLog      true: 构建命令后 Log打印命令日志;  false :不打印命令日志
     * @return cmds
     */
    fun getBoxblur(
        inputPath: String,
        outputPath: String,
        boxblur: String?,
        isLog: Boolean
    ): Array<String> {
        val cmdlist = RxFFmpegCommandList()
        cmdlist.append("-i")
        cmdlist.append(inputPath)
        cmdlist.append("-vf")
        cmdlist.append("boxblur=" + (if (TextUtils.isEmpty(boxblur)) "5:1" else boxblur))
        cmdlist.append("-preset")
        cmdlist.append("superfast")
        cmdlist.append(outputPath)
        return cmdlist.build(isLog)
    }
}
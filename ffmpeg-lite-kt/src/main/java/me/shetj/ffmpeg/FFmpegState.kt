package me.shetj.ffmpeg

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2022/9/14<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
sealed class FFmpegState{

    object OnStart: FFmpegState()

    object OnFinish: FFmpegState()

    class OnProgress(val progress: Int, val progressTime: Long): FFmpegState()

    object OnCancel: FFmpegState()

    class OnError(val message: String?): FFmpegState()

}

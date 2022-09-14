package me.shetj.ffmpeg.kt

/**
 *
 * <b>@author：</b> shetj<br>
 * <b>@createTime：</b> 2022/9/14<br>
 * <b>@email：</b> 375105540@qq.com<br>
 * <b>@describe</b>  <br>
 */
sealed class RunState{

    object OnStart:RunState()

    object OnFinish:RunState()

    class OnProgress(val progress: Int, val progressTime: Long):RunState()

    object OnCancel:RunState()

    class OnError(val message: String?):RunState()

}

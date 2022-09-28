package io.microshow.rxffmpeg.player

import android.view.Surface

/**
 * 播放器基础接口
 * Created by Super on 2020/4/26.
 */
interface IMediaPlayer {
    /**
     * 视频画面承载
     *
     * @param surface surface
     */
    fun setSurface(surface: Surface?)

    /**
     * 通过一个具体的路径来设置MediaPlayer的数据源，path可以是本地的一个路径，也可以是一个网络路径
     *
     * @param path -
     */
    fun setDataSource(path: String?)

    /**
     * 装载流媒体文件
     */
    fun prepare()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 恢复播放
     */
    fun resume()

    /**
     * 开始
     */
    fun start()

    /**
     * 停止
     */
    fun stop()

    /**
     * 指定播放的位置
     *
     * @param secds -
     */
    fun seekTo(secds: Int)

    /**
     * 得到文件的时间
     *
     * @return -
     */
    val duration: Int
    /**
     * 是否循环播放
     *
     * @return -
     */
    /**
     * 是否循环
     *
     * @param looping -
     */
    var isLooping: Boolean

    /**
     * 是否正在播放
     *
     * @return -
     */
    val isPlaying: Boolean
    /**
     * 获取音量 默认100
     */
    /**
     * 设置音量
     *
     * @param percent 取值范围( 0 - 100 )； 0是静音
     */
    var volume: Int
    /**
     * 获取声道：0立体声；1左声道；2右声道；
     * 如果没有调用setMuteSolo，则返回-1 （默认没有设置）
     *
     * @return
     */
    /**
     * 设置声道；0立体声；1左声道；2右声道
     */
    var muteSolo: Int

    /**
     * 回收流媒体资源
     */
    fun release()

    /**
     * 装载流媒体完毕的时候回调
     */
    interface OnPreparedListener {
        fun onPrepared(mediaPlayer: IMediaPlayer?)
    }

    /**
     * 网络流媒体播放结束时回调
     */
    interface OnCompletionListener {
        fun onCompletion(mediaPlayer: IMediaPlayer?)
    }

    /**
     * 发生错误时回调
     */
    interface OnErrorListener {
        fun onError(mediaPlayer: IMediaPlayer?, err: Int, msg: String)
    }

    /**
     * 加载回调
     */
    interface OnLoadingListener {
        fun onLoading(mediaPlayer: IMediaPlayer?, isLoading: Boolean)
    }

    /**
     * 视频size改变
     */
    interface OnVideoSizeChangedListener {
        fun onVideoSizeChanged(mediaPlayer: IMediaPlayer?, width: Int, height: Int, dar: Float)
    }

    /**
     * 时间更新回调
     */
    interface OnTimeUpdateListener {
        fun onTimeUpdate(mediaPlayer: IMediaPlayer?, currentTime: Int, totalTime: Int)
    }
}
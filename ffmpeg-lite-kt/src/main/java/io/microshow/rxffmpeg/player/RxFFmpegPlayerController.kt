package io.microshow.rxffmpeg.player

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout

/**
 * 抽象的控制层容器
 * Created by Super on 2020/5/4.
 */
internal abstract class RxFFmpegPlayerController(context: Context?) : FrameLayout(
    context!!
) {
    protected var mPlayerView: RxFFmpegPlayerView? = null
    protected var mPlayer: BaseMediaPlayer? = null
    private fun init() {
        LayoutInflater.from(context).inflate(layoutId, this, true)
        initView()
    }

    fun setPlayerView(playerView: RxFFmpegPlayerView?) {
        if (playerView != null) {
            mPlayerView = playerView
            mPlayer = mPlayerView!!.mPlayer
            initListener()
        }
    }

    /**
     * 子类实现 提供 layout id
     *
     * @return -
     */
    protected abstract val layoutId: Int

    /**
     * 初始化view
     */
    protected abstract fun initView()

    /**
     * 设置播放器动作Listener
     */
    protected abstract fun initListener()

    /**
     * 播放器触发了 Pause
     */
    abstract fun onPause()

    /**
     * 播放器触发了 Resume
     */
    abstract fun onResume()

    init {
        init()
    }
}
package io.microshow.rxffmpeg.player

import android.view.TextureView
import io.microshow.rxffmpeg.player.IMediaPlayer.OnLoadingListener
import io.microshow.rxffmpeg.player.IMediaPlayer.OnTimeUpdateListener

/**
 * 基础的 Player
 * Created by Super on 2020/7/14.
 */
internal abstract class BaseMediaPlayer : IMediaPlayer {
    var mOnPreparedListener: IMediaPlayer.OnPreparedListener? = null
    var mOnVideoSizeChangedListener: IMediaPlayer.OnVideoSizeChangedListener? = null
    var mOnLoadingListener: OnLoadingListener? = null
    var mOnTimeUpdateListener: OnTimeUpdateListener? = null
    var mOnErrorListener: IMediaPlayer.OnErrorListener? = null
    var mOnCompletionListener: IMediaPlayer.OnCompletionListener? = null

    /**
     * 设置 TextureView
     *
     * @param textureView textureView
     */
    abstract fun setTextureView(textureView: TextureView?)

    /**
     * 播放 子类快捷实现
     *
     * @param path      path
     * @param isLooping isLooping
     */
    abstract fun play(path: String?, isLooping: Boolean)
    abstract fun repeatPlay()
    open fun setOnPreparedListener(listener: IMediaPlayer.OnPreparedListener?) {
        mOnPreparedListener = listener
    }

    open fun setOnVideoSizeChangedListener(listener: IMediaPlayer.OnVideoSizeChangedListener?) {
        mOnVideoSizeChangedListener = listener
    }

    open fun setOnLoadingListener(listener: OnLoadingListener?) {
        mOnLoadingListener = listener
    }

    open fun setOnTimeUpdateListener(listener: OnTimeUpdateListener?) {
        mOnTimeUpdateListener = listener
    }

    open fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener?) {
        mOnErrorListener = listener
    }

    open fun setOnCompleteListener(listener: IMediaPlayer.OnCompletionListener?) {
        mOnCompletionListener = listener
    }
}
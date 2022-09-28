package io.microshow.rxffmpeg.player

import android.media.MediaPlayer
import android.os.Build
import android.view.Surface
import io.microshow.rxffmpeg.player.IMediaPlayer.OnLoadingListener
import io.microshow.rxffmpeg.player.IMediaPlayer.OnTimeUpdateListener
import kotlinx.coroutines.*
import me.shetj.ffmpeg.defScope
import java.io.IOException

/**
 * 系统播放器 MediaPlayer
 * Created by Super on 2020/7/14.
 */
internal abstract class SystemMediaPlayer : BaseMediaPlayer() {


    private var timeJob: Job? = null
    private val scope: CoroutineScope by defScope()

    var mMediaPlayer: MediaPlayer

    /**
     * 视频路径
     */
    protected var path: String? = null

    /**
     * 音量
     */
    var volumePercent = -1
    override fun setSurface(surface: Surface?) {
        if (surface != null) {
            mMediaPlayer.setSurface(surface)
        }
    }

    override fun setDataSource(path: String?) {
        try {
            this.path = path
            mMediaPlayer.setDataSource(path)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun prepare() {
        mMediaPlayer.prepareAsync()
    }

    override fun pause() {
        mMediaPlayer.pause()
        cancelTimeUpdateDisposable()
    }

    override fun resume() {
        start()
    }

    override fun start() {
        mMediaPlayer.start()
        startTimeUpdateDisposable()
    }

    override fun stop() {
        mMediaPlayer.stop()
    }

    override fun seekTo(secds: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mMediaPlayer.seekTo(secds.toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            mMediaPlayer.seekTo(secds)
        }
    }

    override val duration: Int
        get() = mMediaPlayer.duration
    override var isLooping: Boolean
        get() = mMediaPlayer.isLooping
        set(looping) {
            mMediaPlayer.isLooping = looping
        }
    override val isPlaying: Boolean
        get() = mMediaPlayer.isPlaying
    override var volume: Int
        get() = volumePercent
        set(percent) {
            volumePercent = percent
            mMediaPlayer.setVolume(percent.toFloat() / 100, percent.toFloat() / 100)
        }
    override var muteSolo: Int
        get() = 0
        set(mute) {}

    override fun release() {
        setOnPreparedListener(null)
        setOnVideoSizeChangedListener(null)
        setOnLoadingListener(null)
        setOnTimeUpdateListener(null)
        setOnErrorListener(null)
        setOnCompleteListener(null)
        cancelTimeUpdateDisposable()
        mMediaPlayer.release()
        scope.cancel()
    }

    override fun setOnPreparedListener(listener: IMediaPlayer.OnPreparedListener?) {
        super.setOnPreparedListener(listener)
        mMediaPlayer.setOnPreparedListener { listener?.onPrepared(this@SystemMediaPlayer) }
    }

    override fun setOnVideoSizeChangedListener(listener: IMediaPlayer.OnVideoSizeChangedListener?) {
        super.setOnVideoSizeChangedListener(listener)
        mMediaPlayer.setOnVideoSizeChangedListener { mediaPlayer, width, height ->
            listener?.onVideoSizeChanged(
                this@SystemMediaPlayer,
                width,
                height,
                width.toFloat() / height
            )
        }
    }

    override fun setOnLoadingListener(listener: OnLoadingListener?) {
        super.setOnLoadingListener(listener)
    }

    override fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener?) {
        super.setOnErrorListener(listener)
        mMediaPlayer.setOnErrorListener { mediaPlayer, what, extra ->
            if (listener != null && what != -38) {
                listener.onError(this@SystemMediaPlayer, what, extra.toString() + "")
            }
            true
        }
    }

    override fun setOnCompleteListener(listener: IMediaPlayer.OnCompletionListener?) {
        super.setOnCompleteListener(listener)
        mMediaPlayer.setOnCompletionListener { listener?.onCompletion(this@SystemMediaPlayer) }
    }

    override fun setOnTimeUpdateListener(listener: OnTimeUpdateListener?) {
        super.setOnTimeUpdateListener(listener)
        mMediaPlayer.setOnInfoListener(MediaPlayer.OnInfoListener { mediaPlayer, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START
                || what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING
            ) {
                if (mOnLoadingListener != null) { //隐藏加载圈
                    mOnLoadingListener!!.onLoading(this@SystemMediaPlayer, false)
                }
                return@OnInfoListener true
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                if (mOnLoadingListener != null) { //显示加载圈
                    mOnLoadingListener!!.onLoading(this@SystemMediaPlayer, true)
                }
                return@OnInfoListener true
            }
            false
        })
    }

    override fun repeatPlay() {
        play(path, mMediaPlayer.isLooping)
    }

    /**
     * 取消 时间更新 Disposable
     */
    private fun cancelTimeUpdateDisposable() {
        timeJob?.cancel()
    }

    /**
     * 开始执行时间更新
     */
    fun startTimeUpdateDisposable() {
        cancelTimeUpdateDisposable()
        timeJob = scope.launch {
            while (true) {
                //每 300 毫秒执行进度更新
                mOnTimeUpdateListener?.onTimeUpdate(
                    this@SystemMediaPlayer,
                    mMediaPlayer.currentPosition / 1000,
                    duration / 1000
                )
                delay(300)
            }
        }
    }

    init {
        mMediaPlayer = MediaPlayer()
    }
}
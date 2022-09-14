package io.microshow.rxffmpeg.player

import android.text.TextUtils
import android.view.Surface

/**
 * RxFFmpegPlayer 播放器内核
 * Created by Super on 2020/4/26.
 */
internal abstract class RxFFmpegPlayer : BaseMediaPlayer() {
    companion object {
        init {
            System.loadLibrary("rxffmpeg-core")
            System.loadLibrary("rxffmpeg-player")
        }
    }

    private external fun nativeSetSurface(surface: Surface)
    private external fun nativePrepare(url: String?)
    private external fun nativeStart()
    private external fun nativePause()
    private external fun nativeResume()
    private external fun nativeStop()
    private external fun nativeRelease()
    private external fun nativeSeekTo(position: Int)
    private external fun nativeIsPlaying(): Boolean
    private external fun nativeSetVolume(percent: Int)
    private external fun nativeGetVolume(): Int
    private external fun nativeSetMuteSolo(mute: Int)
    private external fun nativeGetMuteSolo(): Int

    /**
     * 视频路径
     */
    protected var path: String? = null

    /**
     * 总时长
     */
    override var duration = 0
        protected set

    /**
     * 循环标志
     */
    override var isLooping = false

    override fun setSurface(surface: Surface?) {
        surface?.let { nativeSetSurface(it) }
    }

    override fun setDataSource(path: String?) {
        this.path = path
    }

    override fun prepare() {
        if (!TextUtils.isEmpty(path)) {
            nativePrepare(path)
        }
    }

    override fun pause() {
        nativePause()
    }

    override fun resume() {
        nativeResume()
    }

    override fun start() {
        if (!TextUtils.isEmpty(path)) {
            nativeStart()
        }
    }

    override fun stop() {
        cancelTimeDisposable()
        nativeStop()
    }

    override fun seekTo(secds: Int) {
        nativeSeekTo(secds)
    }

    override val isPlaying: Boolean
        get() = nativeIsPlaying()
    override var volume: Int
        get() = nativeGetVolume()
        set(percent) {
            nativeSetVolume(percent)
        }
    override var muteSolo: Int
        get() = nativeGetMuteSolo()
        set(mute) {
            nativeSetMuteSolo(mute)
        }

    override fun release() {
        setOnPreparedListener(null)
        setOnVideoSizeChangedListener(null)
        setOnLoadingListener(null)
        setOnTimeUpdateListener(null)
        setOnErrorListener(null)
        setOnCompleteListener(null)
        nativeRelease()
    }

    /**
     * 重新播放
     */
    override fun repeatPlay() {
        play(path, isLooping)
    }

    /**
     * 取消 延迟时间的 Disposable
     */
    private fun cancelTimeDisposable() {}

    /**
     * 准备状态  由native层回调
     */
    fun onPreparedNative() {
        if (mOnPreparedListener != null) {
            mOnPreparedListener!!.onPrepared(this)
        }
    }

    /**
     * 视频尺寸回调  由native层回调
     *
     * @param width  宽
     * @param height 高
     * @param dar    比例
     */
    fun onVideoSizeChangedNative(width: Int, height: Int, dar: Float) {
        if (mOnVideoSizeChangedListener != null) {
            mOnVideoSizeChangedListener!!.onVideoSizeChanged(this, width, height, dar)
        }
    }

    /**
     * 加载状态 由native层回调
     *
     * @param load -
     */
    fun onLoadingNative(load: Boolean) {
        if (mOnLoadingListener != null) {
            mOnLoadingListener!!.onLoading(this, load)
        }
    }

    /**
     * 时间更新 由native层回调
     *
     * @param currentTime
     * @param totalTime
     */
    fun onTimeUpdateNative(currentTime: Int, totalTime: Int) {
        if (mOnTimeUpdateListener != null) {
            duration = totalTime
            mOnTimeUpdateListener!!.onTimeUpdate(this, currentTime, totalTime)
        }
    }

    /**
     * 错误回调 由native层回调
     *
     * @param code -
     * @param msg  -
     */
    fun onErrorNative(code: Int, msg: String) {
        if (mOnErrorListener != null) {
            mOnErrorListener!!.onError(this, code, msg)
        }
    }

    /**
     * 播放完成 由native层回调
     */
    fun onCompletionNative() {
        if (mOnCompletionListener != null) {
            mOnCompletionListener!!.onCompletion(this)
        }
        if (isLooping) { //循环状态，则延迟重播
            play(path, isLooping)
        }
    }
}
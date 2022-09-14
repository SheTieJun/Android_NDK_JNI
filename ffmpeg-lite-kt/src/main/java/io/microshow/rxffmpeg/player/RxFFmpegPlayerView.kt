package io.microshow.rxffmpeg.player

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import io.microshow.rxffmpeg.player.MeasureHelper.FitModel
import io.microshow.rxffmpeg.player.MeasureHelper.VideoSizeInfo
import java.lang.ref.WeakReference

/**
 * 播放器view
 * Created by Super on 2020/5/4.
 */
internal class RxFFmpegPlayerView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null
) : FrameLayout(
    mContext, attrs
) {
    /**
     * 播放器核心 枚举
     */
    enum class PlayerCoreType {
        /**
         * RxFFmpegPlayer 内核
         */
        PCT_RXFFMPEG_PLAYER,

        /**
         * 系统 MediaPlayer 内核
         */
        PCT_SYSTEM_MEDIA_PLAYER
    }

    /**
     * 默认 RxFFmpegPlayer 内核
     */
    var mPlayerCoreType = PlayerCoreType.PCT_RXFFMPEG_PLAYER
    private val mMeasureHelper: MeasureHelper?
    var containerView: FrameLayout? = null
        private set
    private var mPlayerController: RxFFmpegPlayerController? = null
    var textureView: TextureView? = null
        private set
    var mPlayer: BaseMediaPlayer? = null
    private var mCurrentMode = MODE_NORMAL

    /**
     * 切换播放器内核
     *
     * @param playerCoreType playerCoreType
     */
    fun switchPlayerCore(playerCoreType: PlayerCoreType) {
        mPlayerCoreType = playerCoreType
    }

    /**
     * 初始化播放器
     */
    private fun initPlayer() {
        if (textureView == null) {
            textureView = ScaleTextureView(mContext)
        }
        mPlayer = if (mPlayerCoreType == PlayerCoreType.PCT_SYSTEM_MEDIA_PLAYER) {
            //系统 MediaPlayer 内核
            SystemMediaPlayerImpl()
        } else {
            //RxFFmpegPlayer 内核
            RxFFmpegPlayerImpl()
        }
        mPlayer!!.setTextureView(textureView)
        mPlayer!!.setOnVideoSizeChangedListener(VideoSizeChangedListener(this))
    }

    //更新view尺寸
    private fun updateVideoLayoutParams(width: Int, height: Int, dar: Float) {
        post(object : Runnable {
            override fun run() {
                mMeasureHelper!!.videoSizeInfo = (VideoSizeInfo(width, height, dar))
                mMeasureHelper!!.setVideoLayoutParams(textureView, containerView)
            }
        })
    }

    /**
     * 视频size改变 监听
     */
    class VideoSizeChangedListener internal constructor(mRxFFmpegPlayerView: RxFFmpegPlayerView) :
        IMediaPlayer.OnVideoSizeChangedListener {
        private val mWeakReference: WeakReference<RxFFmpegPlayerView>
        override fun onVideoSizeChanged(
            mediaPlayer: IMediaPlayer?,
            width: Int,
            height: Int,
            dar: Float
        ) {
            val mRxFFmpegPlayerView = mWeakReference.get()
            mRxFFmpegPlayerView?.updateVideoLayoutParams(width, height, dar)
        }

        init {
            mWeakReference = WeakReference(mRxFFmpegPlayerView)
        }
    }

    private fun initContainer() {
        containerView = FrameLayout(mContext)
        containerView!!.setBackgroundColor(Color.BLACK)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.addView(containerView, params)
    }

    /**
     * 设置播放器背景颜色
     *
     * @param color
     */
    fun setPlayerBackgroundColor(color: Int) {
        if (containerView != null) {
            containerView!!.setBackgroundColor(color)
        }
    }

    /**
     * 设置设置控制层容器
     *
     * @param playerController 控制层容器
     * @param fitModel         设置视频尺寸适配模式
     */
    fun setController(playerController: RxFFmpegPlayerController?, fitModel: FitModel?) {
        initPlayer()
        setFitModel(fitModel)
        containerView!!.removeView(mPlayerController)
        mPlayerController = playerController
        mPlayerController!!.setPlayerView(this@RxFFmpegPlayerView)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        containerView!!.addView(mPlayerController, params)
        addTextureView()
    }

    private fun addTextureView() {
        containerView!!.removeView(textureView)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        )
        containerView!!.addView(textureView, 0, params)
    }

    /**
     * 设置 TextureView 是否启用 缩放旋转手势
     *
     * @param enabled true:启用（默认）；false:禁用
     */
    fun setTextureViewEnabledTouch(enabled: Boolean) {
        if (textureView != null && textureView is ScaleTextureView) {
            (textureView as ScaleTextureView).setEnabledTouch(enabled)
        }
    }

    /**
     * 设置播放完成回调
     * @param listener -
     */
    fun setOnCompleteListener(listener: IMediaPlayer.OnCompletionListener?) {
        if (mPlayer != null) {
            mPlayer!!.setOnCompleteListener(listener)
        }
    }

    /**
     * 设置适配模式
     *
     * @param fitModel -
     */
    fun setFitModel(fitModel: FitModel?) {
        if (mMeasureHelper != null && fitModel != null) {
            mMeasureHelper.fitModel = fitModel
            //设置默认的 宽高
            mMeasureHelper.setDefaultVideoLayoutParams()
        }
    }

    /**
     * 播放
     *
     * @param videoPath -
     * @param isLooping -
     */
    fun play(videoPath: String?, isLooping: Boolean) {
        if (mPlayer != null && !Helper.isFastClick) {
            mPlayer!!.play(videoPath, isLooping)
            if (mPlayerController != null) {
                mPlayerController!!.onResume()
            }
        }
    }

    /**
     * 重新播放
     */
    fun repeatPlay() {
        if (mPlayer != null) {
            mPlayer!!.repeatPlay()
        }
    }

    /**
     * 暂停
     */
    fun pause() {
        if (mPlayer != null) {
            mPlayer!!.pause()
            if (mPlayerController != null) {
                mPlayerController!!.onPause()
            }
        }
    }

    /**
     * 恢复播放
     */
    fun resume() {
        if (mPlayer != null) {
            mPlayer!!.resume()
            if (mPlayerController != null) {
                mPlayerController!!.onResume()
            }
        }
    }

    /**
     * 是否在播放
     *
     * @return -
     */
    val isPlaying: Boolean
        get() = mPlayer != null && mPlayer!!.isPlaying

    /**
     * 是否循环
     *
     * @return -
     */
    val isLooping: Boolean
        get() = mPlayer != null && mPlayer!!.isLooping
    //    /**
    //     * 停止
    //     */
    //    public void stop() {
    //        mPlayer.stop();
    //    }
    /**
     * 获取音量
     *
     * @return volume
     */
    /**
     * 设置音量 (需要在play方法之前调用)
     *
     * @param percent 取值范围( 0 - 100 )； 0是静音
     */
    var volume: Int
        get() = if (mPlayer != null) {
            if (mPlayer!!.volume != -1) mPlayer!!.volume else 100
        } else {
            100
        }
        set(percent) {
            if (mPlayer != null) {
                mPlayer!!.volume = (percent)
            }
        }
    /**
     * 获取声道：0立体声；1左声道；2右声道；
     */
    /**
     * 设置声道；0立体声；1左声道；2右声道
     */
    var muteSolo: Int
        get() = if (mPlayer != null) {
            if (mPlayer!!.muteSolo != -1) mPlayer!!.muteSolo else 0
        } else {
            0
        }
        set(mute) {
            if (mPlayer != null) {
                mPlayer!!.muteSolo = (mute)
            }
        }

    /**
     * 销毁
     */
    fun release() {
        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
        keepScreenOn = false //设置屏幕保持常亮
    }

    /**
     * 当前是否是全屏
     *
     * @return true:是；false:否
     */
    val isFullScreenModel: Boolean
        get() = mCurrentMode == MODE_FULL_SCREEN

    /**
     * 切换全屏或关闭全屏
     *
     * @return true已经进入到全屏
     */
    fun switchScreen(): Boolean {
        return if (isFullScreenModel) {
            //是全屏 则退出全屏
            exitFullScreen() //退出全屏
        } else {
            enterFullScreen() //进入全屏
        }
    }

    /**
     * 进入全屏
     */
    fun enterFullScreen(): Boolean {
        if (mCurrentMode == MODE_FULL_SCREEN) return false
        val decorView = Helper.setFullScreen(mContext, true) ?: return false
        removeView(containerView)
        val params = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        decorView.addView(containerView, params)
        mCurrentMode = MODE_FULL_SCREEN
        return true
    }

    /**
     * 退出全屏
     */
    fun exitFullScreen(): Boolean {
        if (mCurrentMode == MODE_FULL_SCREEN) {
            val decorView = Helper.setFullScreen(mContext, false)
                ?: return false
            decorView.removeView(containerView)
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.addView(containerView, params)
            mCurrentMode = MODE_NORMAL
        }
        return false
    }

    //屏幕旋转后改变
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //屏幕旋转后更新layout尺寸
        mMeasureHelper?.setVideoLayoutParams(textureView, containerView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = mMeasureHelper!!.doMeasure(measuredWidth, measuredHeight)
        setMeasuredDimension(size[0], size[1])
    }

    companion object {
        /**
         * 普通模式
         */
        const val MODE_NORMAL = 0

        /**
         * 全屏模式
         */
        const val MODE_FULL_SCREEN = 1
    }

    init {
        mMeasureHelper = object : MeasureHelper(this) {
            override val isFullScreen: Boolean
                get() = mCurrentMode == MODE_FULL_SCREEN
        }
        initContainer()
        keepScreenOn = true //设置屏幕保持常亮
    }
}
package io.microshow.rxffmpeg.player

import android.content.Context
import android.view.View
import android.widget.*
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
import me.shetj.ffmpeg.kt.R
import java.lang.ref.WeakReference

/**
 * 控制层 实现类
 * Created by Super on 2020/5/4.
 */
internal class RxFFmpegPlayerControllerImpl(context: Context?) : RxFFmpegPlayerController(context) {
    private var mTimeView: TextView? = null
    private var mProgressView: SeekBar? = null
    private var mProgressBar: ProgressBar? = null
    private var mBottomPanel: View? = null
    private var playBtn: ImageView? = null
    private var repeatPlay: View? = null
    private var muteImage //静音图标
            : ImageView? = null
    private var isSeeking = false
    var mPosition = 0
    override val layoutId: Int
        get() = R.layout.rxffmpeg_player_controller

    public override fun initView() {
        mBottomPanel = findViewById(R.id.bottomPanel)
        mProgressView = findViewById(R.id.progress_view)
        mTimeView = findViewById(R.id.time_view)
        mProgressBar = findViewById(R.id.progressBar)
        playBtn = findViewById(R.id.iv_play)
        repeatPlay = findViewById(R.id.repeatPlay)
        repeatPlay?.setOnClickListener { //隐藏重播按钮
            mPlayerView!!.repeatPlay()
            repeatPlay!!.visibility = GONE
        }
        val mFullScreenIv = findViewById<View>(R.id.iv_fullscreen)
        mFullScreenIv.setOnClickListener {
            if (mPlayerView != null) {
                //屏幕旋转 全屏
                mPlayerView!!.switchScreen()
            }
        }
        muteImage = findViewById(R.id.iv_mute)
        muteImage?.setOnClickListener(OnClickListener { //静音
            switchMute()
        })
        playBtn?.setOnClickListener(OnClickListener {
            if (mPlayerView != null) {
                if (mPlayerView!!.isPlaying) { //暂停播放
                    mPlayerView!!.pause()
                } else { //恢复播放
                    mPlayerView!!.resume()
                }
            }
        })
    }

    fun switchMute() {
        if (mPlayerView != null) {
            if (mPlayerView!!.volume == 0) {
                //当前是静音，设置为非静音
                mPlayerView!!.volume = 100
                muteImage!!.setImageResource(R.mipmap.rxffmpeg_player_unmute)
            } else {
                //当前不是静音，设置为静音
                mPlayerView!!.volume = 0
                muteImage!!.setImageResource(R.mipmap.rxffmpeg_player_mute)
            }
        }
    }

    public override fun initListener() {
        val mPlayerListener = PlayerListener(this)
        mPlayer!!.setOnLoadingListener(mPlayerListener)
        mPlayer!!.setOnTimeUpdateListener(mPlayerListener)
        mPlayer!!.setOnErrorListener(mPlayerListener)
        mPlayer!!.setOnCompleteListener(mPlayerListener)
        mProgressView!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mPosition = progress * mPlayer!!.duration / 100
                if (isSeeking) {
                    if (mPlayerView!!.mPlayerCoreType == PlayerCoreType.PCT_RXFFMPEG_PLAYER) {
                        // RxFFmpegPlayer 内核 返回的时间单位是秒
                        onTimeUpdate(null, mPosition, mPlayer!!.duration)
                    } else if (mPlayerView!!.mPlayerCoreType == PlayerCoreType.PCT_SYSTEM_MEDIA_PLAYER) {
                        // 系统 MediaPlayer 内核 返回的时间单位是毫秒秒
                        onTimeUpdate(null, mPosition / 1000, mPlayer!!.duration / 1000)
                    }
                    mPlayer!!.seekTo(mPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
                mPlayer!!.pause() //拖动进度条时 暂停播放
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mPlayer!!.resume() //拖动进度条结束后 恢复播放
                mPlayer!!.seekTo(mPosition)
                isSeeking = false
            }
        })
    }

    /**
     * 播放完成
     */
    fun onCompletion(mediaPlayer: IMediaPlayer?) {
        post {
            //                        Toast.makeText(getContext(), "播放完成了", Toast.LENGTH_SHORT).show();
            if (mPlayerView != null && !mPlayerView!!.isLooping) {
                //不是循环模式 显示重新播放页面
                repeatPlay!!.visibility = VISIBLE
            } else {
                repeatPlay!!.visibility = GONE
            }
        }
    }

    /**
     * 播放出错
     */
    fun onError(mediaPlayer: IMediaPlayer?, code: Int, msg: String) {
        post { Toast.makeText(context, "出错了：code=$code, msg=$msg", Toast.LENGTH_SHORT).show() }
    }

    /**
     * 时间更新
     */
    fun onTimeUpdate(mediaPlayer: IMediaPlayer?, currentTime: Int, totalTime: Int) {
        post(Runnable {
            if (totalTime <= 0) {
                //总时长为0 (直播视频)，则隐藏时间进度条
                mBottomPanel!!.visibility = GONE
                return@Runnable
            } else {
                mBottomPanel!!.visibility = VISIBLE
            }
            mTimeView!!.text = (Helper.secdsToDateFormat(currentTime, totalTime)
                    + " / " + Helper.secdsToDateFormat(totalTime, totalTime))
            if (!isSeeking && totalTime > 0) {
                mProgressView!!.progress = currentTime * 100 / totalTime
            }
        })
    }

    /**
     * 加载状态
     */
    fun onLoading(mediaPlayer: IMediaPlayer?, isLoading: Boolean) {
        post { mProgressBar!!.visibility = if (isLoading) VISIBLE else GONE }
    }

    /**
     * 播放器监听
     */
    class PlayerListener internal constructor(playerControllerImpl: RxFFmpegPlayerControllerImpl) :
        IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, OnLoadingListener,
        OnTimeUpdateListener {
        private val mWeakReference: WeakReference<RxFFmpegPlayerControllerImpl>
        override fun onCompletion(mediaPlayer: IMediaPlayer?) {
            val playerControllerImpl = mWeakReference.get()
            playerControllerImpl?.onCompletion(mediaPlayer)
        }

        override fun onError(mediaPlayer: IMediaPlayer?, err: Int, msg: String) {
            val playerControllerImpl = mWeakReference.get()
            playerControllerImpl?.onError(mediaPlayer, err, msg)
        }

        override fun onLoading(mediaPlayer: IMediaPlayer?, isLoading: Boolean) {
            val playerControllerImpl = mWeakReference.get()
            playerControllerImpl?.onLoading(mediaPlayer, isLoading)
        }

        override fun onTimeUpdate(mediaPlayer: IMediaPlayer?, currentTime: Int, totalTime: Int) {
            val playerControllerImpl = mWeakReference.get()
            playerControllerImpl?.onTimeUpdate(mediaPlayer, currentTime, totalTime)
        }

        init {
            mWeakReference = WeakReference(playerControllerImpl)
        }
    }

    public override fun onPause() {
        playBtn!!.setImageResource(R.mipmap.rxffmpeg_player_start)
        playBtn!!.animate().alpha(1f).start() //显示 播放按钮
    }

    public override fun onResume() {
        playBtn!!.setImageResource(R.mipmap.rxffmpeg_player_pause)
        playBtn!!.animate().alpha(1f).start() //隐藏 播放按钮
        //设置静音图标
        if (mPlayerView != null) {
            muteImage!!.setImageResource(if (mPlayerView!!.volume == 0) R.mipmap.rxffmpeg_player_mute else R.mipmap.rxffmpeg_player_unmute)
        }
    }
}
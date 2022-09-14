package io.microshow.rxffmpeg.player

import android.view.Gravity
import android.view.TextureView
import android.view.View
import android.widget.FrameLayout
import java.lang.ref.WeakReference

/**
 * Created by Super on 2020/5/4.
 */
internal open class MeasureHelper(view: View) {
    private val mWeakView: WeakReference<View>?

    /**
     * 设置视频信息
     *
     * @param videoSizeInfo -
     */
    var videoSizeInfo: VideoSizeInfo? = null
    private val mMeasuredWidth = 0
    private var mMeasuredHeight = 0
    /**
     * 获取适配模式
     *
     * @return
     */
    /**
     * 设置适配模式
     *
     * @param fitModel -
     */
    /**
     * 适配模式
     */
    var fitModel = FitModel.FM_DEFAULT

    /**
     * 适配模式
     */
    enum class FitModel {
        /**
         * 默认, 宽铺满，横屏的视频高度自适应，如果是竖屏的视频，高度等于屏幕宽
         */
        FM_DEFAULT,

        /**
         * 全屏，宽铺满 高自适应
         */
        FM_FULL_SCREEN_WIDTH,

        /**
         * 全屏，高铺满 宽自适应
         */
        FM_FULL_SCREEN_HEIGHT,

        /**
         * 宽高比：16：9
         */
        FM_WH_16X9
    }

    class VideoSizeInfo(val width: Int, val height: Int, val dar: Float)

    val view: View?
        get() {
            if (mWeakView != null) {
                val view = mWeakView.get()
                if (view != null) {
                    return view
                }
            }
            return null
        }
    open val isFullScreen: Boolean
        get() = false

    /**
     * 设置默认的播放器容器宽高
     */
    fun setDefaultVideoLayoutParams() {
        val view = view
        var mPlayerView: RxFFmpegPlayerView? = null
        if (view is RxFFmpegPlayerView) {
            mPlayerView = view
            val width: Int //宽
            val height: Int //高
            width = Helper.getScreenWidth(view.getContext())
            height = width * 9 / 16
            videoSizeInfo = VideoSizeInfo(width, height, width.toFloat() / height)
            setVideoLayoutParams(mPlayerView.textureView, mPlayerView.containerView)
        }
    }

    fun setVideoLayoutParams(textureView: TextureView?, container: FrameLayout?) {
        if (textureView == null || container == null || videoSizeInfo == null) {
            return
        }
        val videoWidth = videoSizeInfo!!.width
        val videoHeight = videoSizeInfo!!.height
        val dar = videoSizeInfo!!.dar

        //原始视频宽高比
        val videoAspect = videoWidth.toFloat() / videoHeight
        var viewWidth = Helper.getScreenWidth(view!!.context)
        var viewHeight = 0
        if (isFullScreen) { //全屏
            //高度铺满
            viewHeight = Helper.getScreenHeight(view!!.context)
            //宽度按比例
            viewWidth = (viewHeight * videoAspect).toInt()
        } else { //非全屏
            if (fitModel == FitModel.FM_FULL_SCREEN_WIDTH) {
                //宽铺满，高度按比例
                viewHeight = (viewWidth / videoAspect).toInt()
            } else if (fitModel == FitModel.FM_FULL_SCREEN_HEIGHT) {
                //高度铺满
                viewHeight = Helper.getFullScreenHeight(view!!.context)
                //宽自适应
                viewWidth = (viewHeight * videoAspect).toInt()
            } else if (fitModel == FitModel.FM_WH_16X9) {
                viewHeight = viewWidth * 9 / 16
                viewWidth = (viewHeight * videoAspect).toInt()
            } else {
                if (videoWidth > videoHeight) { //横屏视频
                    //宽铺满，高度按比例
                    viewHeight = (viewWidth / videoAspect).toInt()
                } else if (videoWidth < videoHeight) { //竖屏视频
                    //高铺满 宽自适应
                    viewHeight = viewWidth
                    viewWidth = (viewHeight * videoAspect).toInt()
                } else { //正方形视频
                    viewHeight = viewWidth
                }
            }
        }
        val params = FrameLayout.LayoutParams(viewWidth, viewHeight)
        params.gravity = Gravity.CENTER
        //            LogUtils.d("Aspect: viewWith=" + viewWidth + ", viewHeight=" + viewHeight + ", dar=" + dar);
//            LogUtils.d("Aspect: w=" + videoWidth + ", h=" + videoHeight + ", videoAspect=" + videoAspect);
        textureView.layoutParams = params

        //容器的宽固定铺满状态，高度跟随playerView的高
        val containerParams = FrameLayout.LayoutParams(
            Helper.getScreenWidth(
                view!!.context
            ), viewHeight
        )
        container.layoutParams = containerParams
        mMeasuredHeight = viewHeight
        view!!.requestLayout()
    }

    /**
     * 开始适配
     *
     * @param widthMeasureSpec  -
     * @param heightMeasureSpec -
     */
    fun doMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): IntArray {
        val viewWidth: Int
        val viewHeight: Int
        if (fitModel == FitModel.FM_DEFAULT || fitModel == FitModel.FM_FULL_SCREEN_HEIGHT) {
            viewWidth = widthMeasureSpec
            viewHeight = mMeasuredHeight
        } else {
            viewWidth = widthMeasureSpec
            viewHeight = heightMeasureSpec
        }
        val size = IntArray(2)
        size[0] = viewWidth
        size[1] = viewHeight
        return size
    }

    init {
        mWeakView = WeakReference(view)
    }
}
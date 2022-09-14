package io.microshow.rxffmpeg.player

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.TextureView

/**
 * 带手势缩放的 TextureView 支持双指缩放、平移、旋转
 *
 *
 * 同时也可以换成继承 RelativeLayout 或其它View,这样内部包含的view也会一起跟着变化
 *
 *
 * Created by Super on 2020/5/5.
 */
internal class ScaleTextureView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(
    context!!, attrs, defStyleAttr
) {
    /**
     * 移动X
     */
    private var mTranslationX = 0f

    /**
     * 移动Y
     */
    private var mTranslationY = 0f

    /**
     * 伸缩比例
     */
    private var scale = 1f

    /**
     * 旋转角度
     */
    private var mRotation = 0f

    // 移动过程中临时变量
    private var actionX = 0f
    private var actionY = 0f
    private var spacing = 0f
    private var degree = 0f
    private var moveType // 0=未选择，1=拖动，2=缩放
            = 0

    /**
     * 默认开启缩放 Touch
     */
    private var enabledTouch = true

    //默认启用旋转功能
    private var enabledRotation = true

    //默认启用移动功能
    private var enabledTranslation = true

    //    @Override
    //    public boolean onInterceptTouchEvent(MotionEvent ev) {
    //        getParent().requestDisallowInterceptTouchEvent(true);
    //        return super.onInterceptTouchEvent(ev);
    //    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enabledTouch) { //整体 Touch 开关
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    moveType = 1
                    actionX = event.rawX
                    actionY = event.rawY
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    moveType = 2
                    spacing = getSpacing(event)
                    degree = getDegree(event)
                }
                MotionEvent.ACTION_MOVE -> if (moveType == 1) {
                    if (enabledTranslation) { //启用了移动操作
                        mTranslationX = mTranslationX + event.rawX - actionX
                        mTranslationY = mTranslationY + event.rawY - actionY
                        setTranslationX(mTranslationX)
                        setTranslationY(mTranslationY)
                        actionX = event.rawX
                        actionY = event.rawY
                    }
                } else if (moveType == 2) {
                    //启用缩放操作
                    scale = scale * getSpacing(event) / spacing
                    scaleX = scale
                    scaleY = scale
                    if (enabledRotation) { //启用了旋转操作
                        mRotation = mRotation + getDegree(event) - degree
                        if (mRotation > 360) {
                            mRotation -= 360
                        }
                        if (mRotation < -360) {
                            mRotation = mRotation + 360
                        }
                        setRotation(mRotation)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> moveType = 0
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 触碰两点间距离, 通过三角函数得到两点间的距离
     */
    private fun getSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /**
     * 取旋转角度. 得到两个手指间的旋转角度
     */
    private fun getDegree(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = Math.atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    /**
     * 整体手势开关设置
     * 设置是否启用 Touch true:启用（默认）；false:禁用
     */
    fun setEnabledTouch(enabled: Boolean) {
        enabledTouch = enabled
    }

    /**
     * 设置是否启用旋转功能 true:启用（默认）；false:禁用
     */
    fun setEnabledRotation(enabled: Boolean) {
        enabledRotation = enabled
    }

    /**
     * 设置是否启用移动功能 true:启用（默认）；false:禁用
     */
    fun setEnabledTranslation(enabled: Boolean) {
        enabledTranslation = enabled
    }

    /**
     * 重置为最原始默认状态
     *
     * @param saveEnabled 是否保留已经开启或禁用的 移动 旋转 缩放动作
     * true：保留；false:不保留
     */
    fun reset(saveEnabled: Boolean) {
        mTranslationX = 0f
        mTranslationY = 0f
        scale = 1f
        mRotation = 0f

        // 移动过程中临时变量
        actionX = 0f
        actionY = 0f
        spacing = 0f
        degree = 0f
        moveType = 0
        if (!saveEnabled) {
            //enabled 开关
            enabledTouch = true
            enabledRotation = true
            enabledTranslation = true
        }
        scaleX = 1.0f
        scaleY = 1.0f
        setRotation(0f)
        setTranslationX(0f)
        setTranslationY(0f)
    }

    init {
        isClickable = true
    }
}
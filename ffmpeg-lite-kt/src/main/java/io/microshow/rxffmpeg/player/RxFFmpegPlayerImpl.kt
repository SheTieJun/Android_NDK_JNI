package io.microshow.rxffmpeg.player

import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.view.Surface
import android.view.TextureView
import io.microshow.rxffmpeg.player.IMediaPlayer.OnLoadingListener
import java.lang.ref.WeakReference

/**
 * RxFFmpegPlayer 实现类
 * Created by Super on 2020/4/28.
 */
internal class RxFFmpegPlayerImpl : RxFFmpegPlayer(), TextureView.SurfaceTextureListener {
    private var mWeakTextureView: WeakReference<TextureView>? = null
    override fun setTextureView(textureView: TextureView?) {
        if (textureView != null) {
            mWeakTextureView = WeakReference(textureView)
            textureView.surfaceTextureListener = this
        }
    }

    private fun getTextureView(): TextureView? {
        if (mWeakTextureView != null) {
            val view = mWeakTextureView!!.get()
            if (view != null) {
                return view
            }
        }
        return null
    }

    override fun play(path: String?, isLooping: Boolean) {
        if (!TextUtils.isEmpty(path)) {
            stop()
            setDataSource(path)
            this.isLooping = (isLooping)
            setOnLoadingListener(object :OnLoadingListener{
                override fun onLoading(mediaPlayer: IMediaPlayer?, isLoading: Boolean) {
                    start()
                }

            })
            prepare()
        }
    }

    override fun release() {
        stop()
        super.release()
        if (mSurfaceTexture != null) {
            mSurfaceTexture!!.release()
            mSurfaceTexture = null
        }
    }

    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
        if (getTextureView() == null) {
            return
        }
        // 解决 暂停状态下，旋转屏幕出现黑屏情况，
        // 因旋转屏幕后会重新生成一个surfaceTexture，
        // 这里把mSurfaceTexture保存起来，旋转后直接恢复第一个surfaceTexture  mTextureView.setSurfaceTexture(mSurfaceTexture);
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surfaceTexture
            setSurface(Surface(mSurfaceTexture))
        } else {
            if (getTextureView() != null) {
                getTextureView()!!.setSurfaceTexture(mSurfaceTexture!!)
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {}
    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}

    companion object {
        private var mSurfaceTexture: SurfaceTexture? = null
    }
}
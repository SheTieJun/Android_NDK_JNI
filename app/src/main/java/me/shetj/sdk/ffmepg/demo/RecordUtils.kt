
package me.shetj.sdk.ffmepg.demo

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import me.shetj.player.PlayerListener
import me.shetj.recorder.core.*
import me.shetj.recorder.mixRecorder.buildMix

/**
 * 录音工具类
 */
class RecordUtils(
    private val context: Context,
    private val callBack: SimRecordListener?
) : RecordListener, PermissionListener,PCMListener {

    private var isOpenWebRtcNS: Boolean = false
    private var bgmUrl: Uri? = null
    private var listener: PlayerListener? = null
    private var recorderType: BaseRecorder.RecorderType = BaseRecorder.RecorderType.MIX

    val TIME = 5 * 60 * 1000L

    val isRecording: Boolean
        get() {
            return mRecorder?.state == RecordState.RECORDING
        }

    fun hasRecord(): Boolean {
        return if (mRecorder != null) {
            mRecorder?.duration!! > 0 && mRecorder!!.state != RecordState.STOPPED
        } else {
            false
        }
    }

    init {
        initRecorder()
    }


    private var startTime: Long = 0 //秒 s
    private var mRecorder: BaseRecorder? = null
    private var saveFile = ""
    val recorderLiveDate: MutableLiveData<BaseRecorder.RecorderType> = MutableLiveData()

    @JvmOverloads
    fun startOrPause(file: String = "") {
        if (mRecorder == null) {
            initRecorder()
        }
        when (mRecorder?.state) {
            RecordState.STOPPED -> {
                if (TextUtils.isEmpty(file)) {
                    val mRecordFile =
                        context.cacheDir.absolutePath + "/" + System.currentTimeMillis() + ".mp3"
                    this.saveFile = mRecordFile
                } else {
                    this.saveFile = file
                }
                mRecorder?.setOutputFile(saveFile, !TextUtils.isEmpty(file))
                mRecorder?.start()
            }
            RecordState.PAUSED -> {
                mRecorder?.resume()
            }
            RecordState.RECORDING -> {
                mRecorder?.pause()
            }
            else -> {}
        }
    }



    /**
     * 更新录音模式
     */
    private fun updateRecorderType(recorderType: BaseRecorder.RecorderType) {
        if (hasRecord()) {
            mRecorder?.complete()
        }
        this.recorderType = recorderType
        recorderLiveDate.postValue(recorderType)
        initRecorder()
    }

    /**
     * VOICE_COMMUNICATION 消除回声和噪声问题
     * MIC 麦克风- 因为有噪音问题
     */
    private fun initRecorder() {
        mRecorder = recorder {
            mMaxTime = 5 * 60 * 1000
            isDebug = true
            samplingRate = 48000
            audioSource = MediaRecorder.AudioSource.MIC
            audioChannel = 1
            mp3BitRate = 128
            mp3Quality = 5
            recordListener = this@RecordUtils
            permissionListener = this@RecordUtils
            pcmListener = this@RecordUtils
        }.let {
            it.buildMix(context)
                .also {
                    it.isEnableVBR(false) // 请不要使用，虽然可以正常播放，但是会时间错误获取会错误，暂时没有解决方法
                    it.setFilter(3000, 200)
                }
        }

        if (recorderType == BaseRecorder.RecorderType.ST) {
            mRecorder!!.getSoundTouch().changeUse(true)
            mRecorder!!.getSoundTouch().setPitchSemiTones(10f) //往女声变
            Toast.makeText(context, "变声，不可以使用背景音乐", Toast.LENGTH_LONG).show()
        }
        mRecorder?.setMaxTime(TIME, TIME - 20 * 1000)
        listener?.let { setBackgroundPlayerListener(it) }
        bgmUrl?.let { setBackGroundUrl(context, it) }
    }

    fun startOrPauseBGM() {
        if (mRecorder?.isPlayMusic() == true) {
            if (mRecorder?.isPauseMusic() == true) {
                mRecorder?.resumeMusic()
            } else {
                mRecorder?.pauseMusic()
            }
        } else {
            mRecorder?.startPlayMusic()
        }
    }

    fun setBackgroundPlayerListener(listener: PlayerListener) {
        this.listener = listener
        mRecorder?.setBackgroundMusicListener(listener)
    }

    fun pause() {
        mRecorder?.pause()
    }

    fun clear() {
        mRecorder?.destroy()
    }

    fun reset() {
        mRecorder?.reset()
    }

    /**
     * 设置开始录制时间
     * @param startTime 已经录制的时间
     */
    fun setTime(startTime: Long) {
        mRecorder?.setCurDuration(startTime)
        callBack?.onRecording(startTime , -1)
    }

    /**
     * 设置最大录制时间
     */
    fun setMaxTime(maxTime: Int) {
        mRecorder?.setMaxTime(maxTime.toLong())
    }

    /**
     * 录音异常
     */
    private fun resolveError() {
        if (mRecorder != null && mRecorder!!.isActive) {
            mRecorder!!.complete()
        }
        FileUtils.deleteFile(saveFile)
    }

    /**
     * 停止录音
     */
    fun complete() {
        mRecorder?.complete()
        Log.i("RecordUtils", "complete:$saveFile")
    }

    override fun needPermission() {
        callBack?.needPermission()
    }

    override fun onStart() {
        callBack?.onStart()
    }

    override fun onResume() {
        callBack?.onStart()
    }

    override fun onReset() {
    }

    override fun onRecording(time: Long, volume: Int) {
        callBack?.onRecording((startTime + time) , volume)
    }

    override fun onPause() {
        callBack?.onPause()
    }

    override fun onRemind(duration: Long) {
        callBack?.onRemind(duration)
    }

    override fun onSuccess(isAutoComplete: Boolean, file: String, time: Long) {
        callBack?.onSuccess(isAutoComplete, file, (time / 1000))
    }

    override fun onMaxChange(time: Long) {
        callBack?.onMaxChange(time / 1000)
    }

    override fun onError(e: Exception) {
        resolveError()
        callBack?.onError(e)
    }

    fun setVolume(volume: Float) {
        mRecorder?.setBGMVolume(volume)
    }

    fun setBackGroundUrl(context: Context?, url: Uri) {
        if (context != null) {
            this.bgmUrl = url
            mRecorder!!.setAudioChannel(AudioUtils.getAudioChannel(context, url))
            mRecorder!!.setBackgroundMusic(context, url, null)
        }
    }

    override fun onBeforePCMToMp3(pcm: ShortArray): ShortArray {
        if (isOpenWebRtcNS){
            WebRtcNsKit.noiseSuppressionByShort(pcm)
        }
        return pcm
    }

    fun setWebRtcNS(b: Boolean) {
        this.isOpenWebRtcNS = b
    }

}

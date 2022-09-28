package io.microshow.rxffmpeg



/**
 * RxFFmpegInvoke 核心类
 * Created by Super on 2018/6/14.
 * 进行包装
 */
internal class RxFFmpegInvoke private constructor() {
    companion object {
        val TAG = RxFFmpegInvoke::class.java.simpleName

        init {
            System.loadLibrary("rxffmpeg-core")
            System.loadLibrary("rxffmpeg-invoke")
        }

        @Volatile
        private var instance: RxFFmpegInvoke? = null


        fun getInstance(): RxFFmpegInvoke {
            return instance ?: synchronized(RxFFmpegInvoke::class.java) {
                RxFFmpegInvoke().also {
                    instance = it
                }
            }
        }


    }
    /**
     * 设置执行监听
     *
     * @param ffmpegListener
     */
    /**
     * ffmpeg 回调监听
     */
    var fFmpegListener: IFFmpegListener? = null

    /**
     * 同步执行 (可以结合RxJava)
     *
     * @param command
     * @param mffmpegListener
     * @return
     */
    fun runCommand(command: Array<String>, mffmpegListener: IFFmpegListener?): Int {
        fFmpegListener = mffmpegListener
        mffmpegListener?.onStart()
        var ret: Int
        synchronized(RxFFmpegInvoke::class.java) {
            ret = runFFmpegCmd(command)
            onClean()
            return ret
        }
    }

    /**
     * 执行ffmpeg cmd
     *
     * @param commands
     * @return
     */
    external fun runFFmpegCmd(commands: Array<String>): Int

    /**
     * 退出，中断当前执行的cmd
     */
    external fun exit()

    /**
     * 设置是否处于调试状态
     *
     * @param debug
     */
    external fun setDebug(debug: Boolean)

    /**
     * 获取媒体文件信息
     *
     * @param filePath 音视频路径
     * @return info
     */
    external fun getMediaInfo(filePath: String?): String?

    /**
     * 内部进度回调
     *
     * @param progress     执行进度
     * @param progressTime 执行的时间，相对于总时间 单位：微秒
     */
    fun onProgress(progress: Int, progressTime: Long) {
        if (fFmpegListener != null) {
            fFmpegListener!!.onProgress(progress, progressTime)
        }
    }

    /**
     * 执行完成
     */
    fun onFinish() {
        if (fFmpegListener != null) {
            fFmpegListener!!.onFinish()
        }
    }

    /**
     * 执行取消
     */
    fun onCancel() {
        if (fFmpegListener != null) {
            fFmpegListener!!.onCancel()
        }
    }

    /**
     * 执行出错
     *
     * @param message
     */
    fun onError(message: String?) {
        if (fFmpegListener != null) {
            fFmpegListener!!.onError(message)
        }
    }

    /**
     * 清除
     */
    fun onClean() {
        //解决内存泄露
        if (fFmpegListener != null) {
            fFmpegListener = null
        }
    }

    /**
     * 销毁实例
     */
    fun onDestroy() {
        if (fFmpegListener != null) {
            fFmpegListener = null
        }
        if (instance != null) {
            instance = null
        }
    }

    /**
     * IFFmpegListener监听接口
     */
    interface IFFmpegListener {

        /**
         * On start
         * 开始执行
         */
        fun onStart()

        /**
         * 执行完成
         */
        fun onFinish()

        /**
         * 进度回调
         *
         * @param progress     执行进度
         * @param progressTime 执行的时间，相对于总时间 单位：微秒
         */
        fun onProgress(progress: Int, progressTime: Long)

        /**
         * 执行取消
         */
        fun onCancel()

        /**
         * 执行出错
         *
         * @param message
         */
        fun onError(message: String?)
    }

}
package me.shetj.ffmpeg.kt

import io.microshow.rxffmpeg.RxFFmpegInvoke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object FFmpegKit {

    private val fFmpegInvoke by lazy { RxFFmpegInvoke.getInstance()  }

    fun setDebug(isDebug: Boolean){
        fFmpegInvoke.setDebug(isDebug)
    }


    fun runCommand(command: Array<String>): Flow<RunState> {
        return  callbackFlow {

            kotlin.runCatching {
                fFmpegInvoke.runCommand(command,object :RxFFmpegInvoke.IFFmpegListener{
                    override fun onStart() {
                        trySend(RunState.OnStart)
                    }

                    override fun onFinish() {
                        trySend(RunState.OnFinish)
                        close()
                    }

                    override fun onProgress(progress: Int, progressTime: Long) {
                        trySend(RunState.OnProgress(progress, progressTime))
                    }

                    override fun onCancel() {
                        trySend(RunState.OnCancel)
                        close()
                    }

                    override fun onError(message: String?) {
                        trySend(RunState.OnError(message))
                        close()
                    }
                })
            }.onFailure {
                trySend(RunState.OnError(it.message))
                close()
            }
        }
    }

    fun cancel(){
        fFmpegInvoke.exit()
    }
}
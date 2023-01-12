package me.shetj.ffmpeg

import android.util.Log
import io.microshow.rxffmpeg.RxFFmpegInvoke
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.suspendCancellableCoroutine

object FFmpegKit {

    private val ffmpegInvoke
        get() = RxFFmpegInvoke.getInstance()

    fun setDebug(isDebug: Boolean) {
        ffmpegInvoke.setDebug(isDebug)
    }


    fun runCommandFlow(command: Array<String>): Flow<FFmpegState> {
        return callbackFlow {
            kotlin.runCatching {
                ffmpegInvoke.runCommand(command, object : RxFFmpegInvoke.IFFmpegListener {
                    override fun onStart() {
                        trySend(FFmpegState.OnStart)
                    }

                    override fun onFinish() {
                        trySend(FFmpegState.OnFinish)
                        close()
                    }

                    override fun onProgress(progress: Int, progressTime: Long) {
                        trySend(FFmpegState.OnProgress(progress, progressTime))
                    }

                    override fun onCancel() {
                        trySend(FFmpegState.OnCancel)
                        close()
                    }

                    override fun onError(message: String?) {
                        trySend(FFmpegState.OnError(message))
                        close()
                    }
                })
            }.onFailure {
                trySend(FFmpegState.OnError(it.message))
                close()
            }

            awaitClose {
                Log.i("FFmpeg", "runCommand close")
            }
        }.catch { cause: Throwable ->
            Log.e("FFmpeg", cause.stackTraceToString())
        }
    }

    suspend fun runCommand(command: Array<String>): FFmpegState {
        return suspendCancellableCoroutine {
            ffmpegInvoke.runCommand(command, object : RxFFmpegInvoke.IFFmpegListener {
                override fun onStart() {
                }

                override fun onFinish() {
                    it.resumeWith(Result.success(FFmpegState.OnFinish))
                }

                override fun onProgress(progress: Int, progressTime: Long) {
                }

                override fun onCancel() {
                    it.resumeWith(Result.success(FFmpegState.OnCancel))
                }

                override fun onError(message: String?) {
                    it.resumeWith(Result.success(FFmpegState.OnError(message)))
                }
            })
        }
    }

    fun cancel() {
        ffmpegInvoke.exit()
        ffmpegInvoke.onClean()
    }


    fun onDestroy() {
        ffmpegInvoke.onDestroy()
    }

}
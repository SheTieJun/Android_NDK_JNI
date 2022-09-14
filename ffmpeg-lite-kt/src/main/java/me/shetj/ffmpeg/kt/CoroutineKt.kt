package me.shetj.ffmpeg.kt

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class DefCoroutineScope : CoroutineScope

class CoroutineScopeImpl(
    override val coroutineContext: CoroutineContext
) : DefCoroutineScope(), LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (source.lifecycle.currentState <= Lifecycle.State.DESTROYED) {
            source.lifecycle.removeObserver(this)
            coroutineContext.cancel()
        }
    }
}

fun defScope() = lazy { CoroutineScopeImpl(coroutineContext()) }

fun coroutineContext() = SupervisorJob() + Dispatchers.Main.immediate
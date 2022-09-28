package me.shetj.ffmpeg

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class DefCoroutineScope : CoroutineScope

class CoroutineScopeImpl(
    override val coroutineContext: CoroutineContext
) : DefCoroutineScope()

fun defScope() = lazy { CoroutineScopeImpl(coroutineContext()) }

fun coroutineContext() = SupervisorJob() + Dispatchers.Main.immediate
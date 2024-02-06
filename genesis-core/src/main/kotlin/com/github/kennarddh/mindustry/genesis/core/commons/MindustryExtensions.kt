package com.github.kennarddh.mindustry.genesis.core.commons

import arc.Core
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun <T> runOnMindustryThread(timeout: Duration = 5.seconds, task: () -> T): T =
    withTimeout(timeout) {
        suspendCancellableCoroutine { continuation ->
            Core.app.post {
                runCatching(task)
                    .fold(continuation::resume, continuation::resumeWithException)
            }
        }
    }
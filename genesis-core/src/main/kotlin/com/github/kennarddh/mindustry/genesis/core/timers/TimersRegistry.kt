package com.github.kennarddh.mindustry.genesis.core.timers

import arc.util.Timer
import com.github.kennarddh.mindustry.genesis.core.commons.CoroutineScopes
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.timers.annotations.TimerTask
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible


class TimersRegistry {
    internal fun init() {
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val timerTaskAnnotation = function.findAnnotation<TimerTask>() ?: continue

            val delaySeconds = timerTaskAnnotation.delaySeconds
            val intervalSeconds = timerTaskAnnotation.intervalSeconds
            val repeatCount = timerTaskAnnotation.repeatCount

            if (timerTaskAnnotation.runOnCallerThread) {
                Timer.schedule({
                    runBlocking {
                        function.callSuspend(handler)
                    }
                }, delaySeconds, intervalSeconds, repeatCount)
            } else {
                Timer.schedule({
                    CoroutineScopes.Main.launch {
                        function.callSuspend(handler)
                    }
                }, delaySeconds, intervalSeconds, repeatCount)
            }
        }
    }
}
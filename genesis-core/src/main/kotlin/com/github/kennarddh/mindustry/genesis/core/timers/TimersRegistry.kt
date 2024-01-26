package com.github.kennarddh.mindustry.genesis.core.timers

import arc.util.Timer
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.timers.annotations.TimerTask
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

            Timer.schedule({ function.call(handler) }, delaySeconds, intervalSeconds, repeatCount)
        }
    }
}
package com.github.kennarddh.mindustry.genesis.core.events

import arc.Events
import com.github.kennarddh.mindustry.genesis.core.commons.CoroutineScopes
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandlerTrigger
import com.github.kennarddh.mindustry.genesis.core.events.exceptions.InvalidEventHandlerMethodException
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

class EventRegistry {
    internal fun init() {
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val eventHandlerAnnotation = function.findAnnotation<EventHandler>() ?: continue

            val eventHandlerTriggerAnnotation = function.findAnnotation<EventHandlerTrigger>()

            val functionParameters = function.parameters.drop(1)

            if (functionParameters.isEmpty() && eventHandlerTriggerAnnotation == null)
                throw InvalidEventHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must have EventHandlerTrigger annotation because it doesn't have any parameter")

            if (functionParameters.size != 1 && eventHandlerTriggerAnnotation == null)
                throw InvalidEventHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept exactly one parameter with the event type or use EventHandlerTrigger annotation")

            if (eventHandlerTriggerAnnotation != null) {
                if (eventHandlerAnnotation.runOnCallerThread) {
                    Events.run(eventHandlerTriggerAnnotation.trigger) {
                        runBlocking {
                            function.callSuspend(handler)
                        }
                    }
                } else {
                    Events.run(eventHandlerTriggerAnnotation.trigger) {
                        CoroutineScopes.Main.launch {
                            function.callSuspend(handler)
                        }
                    }
                }
            } else {
                if (eventHandlerAnnotation.runOnCallerThread) {
                    Events.on((functionParameters[0].type.classifier as KClass<*>).java) {
                        runBlocking {
                            function.callSuspend(handler, it)
                        }
                    }
                } else {
                    Events.on((functionParameters[0].type.classifier as KClass<*>).java) {
                        CoroutineScopes.Main.launch {
                            function.callSuspend(handler, it)
                        }
                    }
                }
            }
        }
    }
}
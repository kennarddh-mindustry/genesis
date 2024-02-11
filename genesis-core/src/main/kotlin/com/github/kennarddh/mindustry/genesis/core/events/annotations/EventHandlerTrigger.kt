package com.github.kennarddh.mindustry.genesis.core.events.annotations

import mindustry.game.EventType.Trigger

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class EventHandlerTrigger(val trigger: Trigger)

package com.github.kennarddh.mindustry.genesis.core.commands.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Command(val names: Array<String>)
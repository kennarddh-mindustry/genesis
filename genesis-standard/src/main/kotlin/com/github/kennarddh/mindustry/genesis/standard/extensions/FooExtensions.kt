package com.github.kennarddh.mindustry.genesis.standard.extensions

import com.github.kennarddh.mindustry.genesis.core.GenesisAPI
import com.github.kennarddh.mindustry.genesis.standard.handlers.foo.FooHandler
import mindustry.gen.Player

fun String.stripFooMessageInvisibleCharacters(): String =
    if (this.takeLast(2).all { (0xF80..<0x107F).contains(it.code) })
        this.dropLast(2)
    else
        this

fun Player.isUsingFooClient() = GenesisAPI.getHandler<FooHandler>()!!.playersWithFoo.contains(this)
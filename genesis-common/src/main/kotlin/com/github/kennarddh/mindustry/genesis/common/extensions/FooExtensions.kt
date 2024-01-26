package com.github.kennarddh.mindustry.genesis.common.extensions

fun String.stripFooMessageInvisibleCharacters(): String =
    if (this.takeLast(2).all { (0xF80..<0x107F).contains(it.code) })
        this.dropLast(2)
    else
        this

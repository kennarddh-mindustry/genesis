package com.github.kennarddh.mindustry.genesis.common.extensions

import mindustry.gen.Call
import mindustry.gen.Player

fun Player.infoMessage(message: String) {
    Call.infoMessage(this.con, message)
}

fun Player.infoPopup(message: String, duration: Float, align: Int, top: Int, left: Int, bottom: Int, right: Int) {
    Call.infoPopup(this.con, message, duration, align, top, left, bottom, right)
}
package com.github.kennarddh.mindustry.genesis.standard.extensions

import arc.audio.Sound
import arc.graphics.Color
import mindustry.entities.Effect
import mindustry.game.MapObjectives
import mindustry.game.Rules
import mindustry.gen.Call
import mindustry.gen.KickCallPacket
import mindustry.gen.Player
import mindustry.net.NetConnection

fun NetConnection.announce(
    message: String,
) {
    Call.announce(this, message)
}

fun NetConnection.clientPacketReliable(
    type: String,
    contents: String,
) {
    Call.clientPacketReliable(this, type, contents)
}

fun NetConnection.clientPacketUnreliable(
    type: String,
    contents: String,
) {
    Call.clientPacketUnreliable(this, type, contents)
}

fun NetConnection.effect(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color
) {
    Call.effect(this, effect, x, y, rotation, color)
}

fun NetConnection.effect(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color,
    data: Any
) {
    Call.effect(this, effect, x, y, rotation, color, data)
}

fun NetConnection.effectReliable(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color
) {
    Call.effectReliable(this, effect, x, y, rotation, color)
}

fun NetConnection.followUpMenu(
    menuID: Int,
    title: String,
    message: String,
    options: Array<Array<String>>
) {
    Call.followUpMenu(this, menuID, title, message, options)
}

fun NetConnection.hideFollowUpMenu(menuID: Int) {
    Call.hideFollowUpMenu(this, menuID)
}

fun NetConnection.hideHudText() {
    Call.hideHudText(this)
}

fun NetConnection.infoMessage(message: String) {
    Call.infoMessage(this, message)
}

fun NetConnection.infoPopup(
    message: String,
    duration: Float,
    align: Int,
    top: Int,
    left: Int,
    bottom: Int,
    right: Int
) {
    Call.infoPopup(this, message, duration, align, top, left, bottom, right)
}

fun NetConnection.infoPopupReliable(
    message: String,
    duration: Float,
    align: Int,
    top: Int,
    left: Int,
    bottom: Int,
    right: Int
) {
    Call.infoPopupReliable(this, message, duration, align, top, left, bottom, right)
}

fun NetConnection.infoToast(message: String, duration: Float) {
    Call.infoToast(this, message, duration)
}

fun NetConnection.label(message: String, duration: Float, worldX: Float, worldY: Float) {
    Call.label(this, message, duration, worldX, worldY)
}

fun NetConnection.labelReliable(message: String, duration: Float, worldX: Float, worldY: Float) {
    Call.labelReliable(this, message, duration, worldX, worldY)
}

fun NetConnection.menu(
    menuID: Int,
    title: String,
    message: String,
    options: Array<Array<String>>
) {
    Call.menu(this, menuID, title, message, options)
}

fun NetConnection.openURI(uri: String) {
    Call.openURI(this, uri)
}

fun NetConnection.removeQueueBlock(x: Int, y: Int, breaking: Boolean) {
    Call.removeQueueBlock(this, x, y, breaking)
}

fun NetConnection.sendMessage(message: String, unformatted: String, sender: Player) {
    Call.sendMessage(this, message, unformatted, sender)
}

fun NetConnection.setCameraPosition(x: Float, y: Float) {
    Call.setCameraPosition(this, x, y)
}

fun NetConnection.setHudText(message: String) {
    Call.setHudText(this, message)
}

fun NetConnection.setHudTextReliable(message: String) {
    Call.setHudTextReliable(this, message)
}

fun NetConnection.setObjectives(mapObjectives: MapObjectives) {
    Call.setObjectives(this, mapObjectives)
}

fun NetConnection.setPosition(x: Float, y: Float) {
    Call.setPosition(this, x, y)
}

fun NetConnection.setRules(rules: Rules) {
    Call.setRules(this, rules)
}

fun NetConnection.sound(sound: Sound, volume: Float, pitch: Float, pan: Float) {
    Call.sound(this, sound, volume, pitch, pan)
}

fun NetConnection.soundAt(sound: Sound, x: Float, y: Float, volume: Float, pitch: Float) {
    Call.soundAt(this, sound, x, y, volume, pitch)
}

fun NetConnection.textInput(
    textInputId: Int,
    title: String,
    message: String,
    maxLength: Int,
    default: String,
    numeric: Boolean
) {
    Call.textInput(this, textInputId, title, message, maxLength, default, numeric)
}

fun NetConnection.warningToast(
    unicode: Int,
    text: String
) {
    Call.warningToast(this, unicode, text)
}

fun NetConnection.kickWithoutLogging(reason: String) {
    val packet = KickCallPacket()

    packet.reason = reason

    this.send(packet, true)
    this.close()
}
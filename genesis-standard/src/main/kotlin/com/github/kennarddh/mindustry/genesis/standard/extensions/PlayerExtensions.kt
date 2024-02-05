package com.github.kennarddh.mindustry.genesis.standard.extensions

import arc.audio.Sound
import arc.graphics.Color
import mindustry.entities.Effect
import mindustry.game.MapObjectives
import mindustry.game.Rules
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.world.Tile

fun Player.announce(
    message: String,
) {
    Call.announce(this.con, message)
}

fun Player.clientPacketReliable(
    type: String,
    contents: String,
) {
    Call.clientPacketReliable(this.con, type, contents)
}

fun Player.clientPacketUnreliable(
    type: String,
    contents: String,
) {
    Call.clientPacketUnreliable(this.con, type, contents)
}

fun Player.effect(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color
) {
    Call.effect(this.con, effect, x, y, rotation, color)
}

fun Player.effect(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color,
    data: Any
) {
    Call.effect(this.con, effect, x, y, rotation, color, data)
}

fun Player.effectReliable(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color
) {
    Call.effectReliable(this.con, effect, x, y, rotation, color)
}

fun Player.followUpMenu(
    menuID: Int,
    title: String,
    message: String,
    options: Array<Array<String>>
) {
    Call.followUpMenu(this.con, menuID, title, message, options)
}

fun Player.hideFollowUpMenu(menuID: Int) {
    Call.hideFollowUpMenu(this.con, menuID)
}

fun Player.hideHudText() {
    Call.hideHudText(this.con)
}

fun Player.infoMessage(message: String) {
    Call.infoMessage(this.con, message)
}

fun Player.infoPopup(message: String, duration: Float, align: Int, top: Int, left: Int, bottom: Int, right: Int) {
    Call.infoPopup(this.con, message, duration, align, top, left, bottom, right)
}

fun Player.infoPopupReliable(
    message: String,
    duration: Float,
    align: Int,
    top: Int,
    left: Int,
    bottom: Int,
    right: Int
) {
    Call.infoPopupReliable(this.con, message, duration, align, top, left, bottom, right)
}

fun Player.infoToast(message: String, duration: Float) {
    Call.infoToast(this.con, message, duration)
}

fun Player.label(message: String, duration: Float, worldX: Float, worldY: Float) {
    Call.label(this.con, message, duration, worldX, worldY)
}

fun Player.labelReliable(message: String, duration: Float, worldX: Float, worldY: Float) {
    Call.labelReliable(this.con, message, duration, worldX, worldY)
}

fun Player.menu(
    menuID: Int,
    title: String,
    message: String,
    options: Array<Array<String>>
) {
    Call.menu(this.con, menuID, title, message, options)
}

fun Player.openURI(uri: String) {
    Call.openURI(this.con, uri)
}

fun Player.spawn(tile: Tile) {
    Call.playerSpawn(tile, this)
}

fun Player.removeQueueBlock(x: Int, y: Int, breaking: Boolean) {
    Call.removeQueueBlock(this.con, x, y, breaking)
}

fun Player.sendMessage(message: String, unformatted: String, sender: Player) {
    Call.sendMessage(this.con, message, unformatted, sender)
}

fun Player.setCameraPosition(x: Float, y: Float) {
    Call.setCameraPosition(this.con, x, y)
}

fun Player.setHudText(message: String) {
    Call.setHudText(this.con, message)
}

fun Player.setHudTextReliable(message: String) {
    Call.setHudTextReliable(this.con, message)
}

fun Player.setObjectives(mapObjectives: MapObjectives) {
    Call.setObjectives(this.con, mapObjectives)
}

fun Player.setPosition(x: Float, y: Float) {
    Call.setPosition(this.con, x, y)
}

fun Player.setRules(rules: Rules) {
    Call.setRules(this.con, rules)
}

fun Player.sound(sound: Sound, volume: Float, pitch: Float, pan: Float) {
    Call.sound(this.con, sound, volume, pitch, pan)
}

fun Player.soundAt(sound: Sound, x: Float, y: Float, volume: Float, pitch: Float) {
    Call.soundAt(this.con, sound, x, y, volume, pitch)
}

fun Player.textInput(
    textInputId: Int,
    title: String,
    message: String,
    maxLength: Int,
    default: String,
    numeric: Boolean
) {
    Call.textInput(this.con, textInputId, title, message, maxLength, default, numeric)
}

fun Player.warningToast(
    unicode: Int,
    text: String
) {
    Call.warningToast(this.con, unicode, text)
}

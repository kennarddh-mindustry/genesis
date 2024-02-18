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
    this.con.announce(message)
}

fun Player.clientPacketReliable(
    type: String,
    contents: String,
) {
    this.con.clientPacketReliable(type, contents)
}

fun Player.clientPacketUnreliable(
    type: String,
    contents: String,
) {
    this.con.clientPacketUnreliable(type, contents)
}

fun Player.effect(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color
) {
    this.con.effect(effect, x, y, rotation, color)
}

fun Player.effect(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color,
    data: Any
) {
    this.con.effect(effect, x, y, rotation, color, data)
}

fun Player.effectReliable(
    effect: Effect,
    x: Float,
    y: Float,
    rotation: Float,
    color: Color
) {
    this.con.effectReliable(effect, x, y, rotation, color)
}

fun Player.followUpMenu(
    menuID: Int,
    title: String,
    message: String,
    options: Array<Array<String>>
) {
    this.con.followUpMenu(menuID, title, message, options)
}

fun Player.hideFollowUpMenu(menuID: Int) {
    this.con.hideFollowUpMenu(menuID)
}

fun Player.hideHudText() {
    this.con.hideHudText()
}

fun Player.infoMessage(message: String) {
    this.con.infoMessage(message)
}

fun Player.infoPopup(message: String, duration: Float, align: Int, top: Int, left: Int, bottom: Int, right: Int) {
    this.con.infoPopup(message, duration, align, top, left, bottom, right)
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
    this.con.infoPopupReliable(message, duration, align, top, left, bottom, right)
}

fun Player.infoToast(message: String, duration: Float) {
    this.con.infoToast(message, duration)
}

fun Player.label(message: String, duration: Float, worldX: Float, worldY: Float) {
    this.con.label(message, duration, worldX, worldY)
}

fun Player.labelReliable(message: String, duration: Float, worldX: Float, worldY: Float) {
    this.con.labelReliable(message, duration, worldX, worldY)
}

fun Player.menu(
    menuID: Int,
    title: String,
    message: String,
    options: Array<Array<String>>
) {
    this.con.menu(menuID, title, message, options)
}

fun Player.openURI(uri: String) {
    this.con.openURI(uri)
}

fun Player.spawn(tile: Tile) {
    Call.playerSpawn(tile, this)
}

fun Player.removeQueueBlock(x: Int, y: Int, breaking: Boolean) {
    this.con.removeQueueBlock(x, y, breaking)
}

fun Player.sendMessage(message: String, unformatted: String, sender: Player) {
    this.con.sendMessage(message, unformatted, sender)
}

fun Player.setCameraPosition(x: Float, y: Float) {
    this.con.setCameraPosition(x, y)
}

fun Player.setHudText(message: String) {
    this.con.setHudText(message)
}

fun Player.setHudTextReliable(message: String) {
    this.con.setHudTextReliable(message)
}

fun Player.setObjectives(mapObjectives: MapObjectives) {
    this.con.setObjectives(mapObjectives)
}

fun Player.setPosition(x: Float, y: Float) {
    this.con.setPosition(x, y)
}

fun Player.setRules(rules: Rules) {
    this.con.setRules(rules)
}

fun Player.sound(sound: Sound, volume: Float, pitch: Float, pan: Float) {
    this.con.sound(sound, volume, pitch, pan)
}

fun Player.soundAt(sound: Sound, x: Float, y: Float, volume: Float, pitch: Float) {
    this.con.soundAt(sound, x, y, volume, pitch)
}

fun Player.textInput(
    textInputId: Int,
    title: String,
    message: String,
    maxLength: Int,
    default: String,
    numeric: Boolean
) {
    this.con.textInput(textInputId, title, message, maxLength, default, numeric)
}

fun Player.warningToast(
    unicode: Int,
    text: String
) {
    this.con.warningToast(unicode, text)
}

fun Player.kickWithoutLogging(reason: String) {
    this.con.kickWithoutLogging(reason)
}
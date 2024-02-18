package com.github.kennarddh.mindustry.genesis.standard.handlers.tap

import arc.Events
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.standard.Logger
import com.github.kennarddh.mindustry.genesis.standard.handlers.tap.events.DoubleTap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mindustry.game.EventType
import mindustry.gen.Player
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DoubleTapHandler : Handler() {
    private val playersLastTap: MutableMap<Player, Instant> = mutableMapOf()

    val doubleClickMaxDelay: Duration = 500.milliseconds

    @EventHandler
    private fun onTap(event: EventType.TapEvent) {
        if (!playersLastTap.contains(event.player)) {
            playersLastTap[event.player] = Clock.System.now()

            return
        }

        Logger.info("x: ${Clock.System.now() - playersLastTap[event.player]!!}")

        if (Clock.System.now() - playersLastTap[event.player]!! <= doubleClickMaxDelay) {
            runOnMindustryThread {
                Events.fire(DoubleTap(event.player, event.tile))
            }

            playersLastTap.remove(event.player)
        }

        playersLastTap[event.player] = Clock.System.now()
    }

    @EventHandler
    private fun onPlayerLeave(event: EventType.PlayerLeave) {
        playersLastTap.remove(event.player)
    }
}
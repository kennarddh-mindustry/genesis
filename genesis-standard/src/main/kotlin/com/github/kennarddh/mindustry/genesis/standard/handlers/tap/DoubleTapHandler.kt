package com.github.kennarddh.mindustry.genesis.standard.handlers.tap

import arc.Events
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.standard.handlers.tap.events.DoubleTap
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mindustry.game.EventType
import mindustry.game.EventType.TapEvent
import mindustry.gen.Player
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DoubleTapHandler : Handler {
    private val playersLastTap = ConcurrentHashMap<Player, Instant>()

    private val doubleClickMaxDelay: Duration = 500.milliseconds

    @EventHandler
    private fun onTap(event: TapEvent) {
        // If key is absent it should return null
        playersLastTap.putIfAbsent(event.player, Clock.System.now()) ?: return

        if (Clock.System.now() - playersLastTap[event.player]!! <= doubleClickMaxDelay) {
            playersLastTap.remove(event.player)

            runOnMindustryThread {
                Events.fire(DoubleTap(event.player, event.tile))
            }
        } else {
            playersLastTap[event.player] = Clock.System.now()
        }
    }

    @EventHandler
    private fun onPlayerLeave(event: EventType.PlayerLeave) {
        playersLastTap.remove(event.player)
    }
}
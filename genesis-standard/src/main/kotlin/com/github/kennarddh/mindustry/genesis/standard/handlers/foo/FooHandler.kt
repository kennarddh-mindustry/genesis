package com.github.kennarddh.mindustry.genesis.standard.handlers.foo

import arc.util.serialization.Jval
import com.github.kennarddh.mindustry.genesis.core.GenesisAPI
import com.github.kennarddh.mindustry.genesis.core.commands.ArcCommand
import com.github.kennarddh.mindustry.genesis.core.commands.events.CommandsChanged
import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.packets.annotations.PacketHandler
import com.github.kennarddh.mindustry.genesis.standard.GenesisStandard
import com.github.kennarddh.mindustry.genesis.standard.extensions.clientPacketReliable
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Call
import mindustry.gen.Player
import java.util.concurrent.ConcurrentSkipListSet


class FooHandler : Handler {
    private val version by lazy { Vars.mods.getMod(GenesisStandard::class.java).meta.version }

    val playersWithFoo: ConcurrentSkipListSet<Player> = ConcurrentSkipListSet()

    @EventHandler
    fun onPlayerLeave(event: EventType.PlayerLeave) {
        playersWithFoo.remove(event.player)
    }


    /** Plugin presence check */
    @PacketHandler(["fooCheck"], Priority.Normal, true)
    suspend fun fooCheck(player: Player) {
        playersWithFoo.add(player)

        player.clientPacketReliable("fooCheck", version)

        enableTransmissions(player)
        sendCommands(player)
    }

    /** Client transmission forwarding */
    @PacketHandler(["fooTransmission"], Priority.Normal, true)
    fun fooTransmission(player: Player, content: String) {
        Call.clientPacketReliable("fooTransmission", "${player.id} $content")
    }

    @EventHandler
    private suspend fun onCommandsChanged(event: CommandsChanged) {
        sendCommands()
    }

    /** Informs clients of the transmission forwarding state. When [player] is null, the status is sent to everyone */
    private fun enableTransmissions(player: Player? = null) {
        val enabled = true

        if (player != null)
            player.clientPacketReliable("fooTransmissionEnabled", enabled.toString())
        else
            Call.clientPacketReliable("fooTransmissionEnabled", enabled.toString())
    }

    /** Sends the list of commands to a player */
    private suspend fun sendCommands(player: Player? = null) {
        with(Jval.newObject()) {
            add("prefix", GenesisAPI.commandRegistry.clientPrefix)

            add("commands", Jval.newObject().apply {
                GenesisAPI.commandRegistry.clientCommands.forEach {
                    val name = if (it is ArcCommand) it.realName else it.text
                    val usage = if (it is ArcCommand) it.toUsage() else it.paramText

                    add(name, usage)
                }
            })

            if (player == null)
                Call.clientPacketReliable("commandList", this.toString())
            else
                player.clientPacketReliable("commandList", this.toString())
        }
    }
}
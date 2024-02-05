package com.github.kennarddh.mindustry.genesis.standard.handlers.foo

import arc.util.serialization.Jval
import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commands.ArcCommand
import com.github.kennarddh.mindustry.genesis.core.commands.events.CommandsChanged
import com.github.kennarddh.mindustry.genesis.core.events.annotations.EventHandler
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.packets.annotations.PacketHandler
import com.github.kennarddh.mindustry.genesis.standard.extensions.clientPacketReliable
import mindustry.Vars
import mindustry.gen.Call
import mindustry.gen.Player


class FooHandler : Handler() {
    private val version by lazy { Vars.mods.getMod(com.github.kennarddh.mindustry.genesis.standard.GenesisStandard::class.java).meta.version }

    /** @since v1 Plugin presence check */
    @PacketHandler(["fooCheck"])
    fun fooCheck(player: Player) {
        player.clientPacketReliable("fooCheck", version)

        enableTransmissions(player)
        sendCommands(player)
    }

    /** @since v1 Client transmission forwarding */
    @PacketHandler(["fooTransmission"])
    fun fooTransmission(player: Player, content: String) {
        Call.clientPacketReliable("fooTransmission", "${player.id} $content")
    }

    @EventHandler
    private fun onCommandsChanged(event: CommandsChanged) {
        sendCommands()
    }

    /** @since v2 Informs clients of the transmission forwarding state. When [player] is null, the status is sent to everyone */
    private fun enableTransmissions(player: Player? = null) {
        val enabled = true

        if (player != null)
            player.clientPacketReliable("fooTransmissionEnabled", enabled.toString())
        else
            Call.clientPacketReliable("fooTransmissionEnabled", enabled.toString())
    }

    /** @since v2 Sends the list of commands to a player */
    private fun sendCommands(player: Player? = null) {
        with(Jval.newObject()) {
            add("prefix", Genesis.commandRegistry.clientPrefix)

            add("commands", Jval.newObject().apply {
                Genesis.commandRegistry.clientCommands.forEach {
                    val name = if (it is ArcCommand) it.realName else it.text
                    val usage = if (it is ArcCommand) it.usage else it.paramText

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

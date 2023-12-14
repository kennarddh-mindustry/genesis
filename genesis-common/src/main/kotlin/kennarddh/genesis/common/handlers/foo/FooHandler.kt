package kennarddh.genesis.common.handlers.foo

import arc.util.serialization.Jval
import kennarddh.genesis.common.GenesisCommon
import kennarddh.genesis.core.Genesis
import kennarddh.genesis.core.commands.events.CommandsChanged
import kennarddh.genesis.core.events.annotations.EventHandler
import kennarddh.genesis.core.handlers.Handler
import kennarddh.genesis.core.packets.annotations.PacketHandler
import mindustry.Vars
import mindustry.gen.Call
import mindustry.gen.Player


class FooHandler : Handler() {
    private val version by lazy { Vars.mods.getMod(GenesisCommon::class.java).meta.version }

    /** @since v1 Plugin presence check */
    @PacketHandler(["fooCheck"])
    fun fooCheck(player: Player) {
        Call.clientPacketReliable(player.con, "fooCheck", version)

        enableTransmissions(player)
        sendCommands(player)
    }

    /** @since v1 Client transmission forwarding */
    @PacketHandler(["fooTransmission"])
    fun fooTransmission(player: Player, content: String) {
        val output = StringBuilder()

        output.append(player.id).append(" ").append(content)

        Call.clientPacketReliable("fooTransmission", output.toString())
    }

    @EventHandler
    private fun onCommandsChanged(event: CommandsChanged) {
        sendCommands()
    }

    /** @since v2 Informs clients of the transmission forwarding state. When [player] is null, the status is sent to everyone */
    private fun enableTransmissions(player: Player? = null) {
        val enabled = true

        if (player != null)
            Call.clientPacketReliable(player.con, "fooTransmissionEnabled", enabled.toString())
        else
            Call.clientPacketReliable("fooTransmissionEnabled", enabled.toString())
    }

    /** @since v2 Sends the list of commands to a player */
    private fun sendCommands(player: Player? = null) {
        with(Jval.newObject()) {
            add("prefix", Genesis.commandRegistry.clientPrefix)

            add("commands", Jval.newObject().apply {
                Genesis.commandRegistry.clientCommands.forEach {
                    it.names.forEach { name ->
                        add(name, it.toUsage())
                    }
                }
            })

            if (player == null)
                Call.clientPacketReliable("commandList", this.toString())
            else
                Call.clientPacketReliable(player.con, "commandList", this.toString())
        }
    }
}

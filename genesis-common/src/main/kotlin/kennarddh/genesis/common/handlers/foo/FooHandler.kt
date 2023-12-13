package kennarddh.genesis.common.handlers.foo

import kennarddh.genesis.common.GenesisCommon
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
        //TODO: After get command usage and command registry exposed
//        sendCommands(player)
    }

    /** @since v1 Client transmission forwarding */
    @PacketHandler(["fooTransmission"])
    fun fooTransmission(player: Player, content: String) {
        val output = StringBuilder()

        output.append(player.id).append(" ").append(content)

        Call.clientPacketReliable("fooTransmission", output.toString())
    }

    /** @since v2 Informs clients of the transmission forwarding state. When [player] is null, the status is sent to everyone */
    private fun enableTransmissions(player: Player? = null) {
        val enabled = true

        if (player != null)
            Call.clientPacketReliable(player.con, "fooTransmissionEnabled", enabled.toString())
        else
            Call.clientPacketReliable("fooTransmissionEnabled", enabled.toString())
    }

//    /** @since v2 Sends the list of commands to a player */
//    private fun sendCommands(player: Player) {
//        with(Jval.newObject()) {
//            add("prefix", Reflect.get<String>(Vars.netServer.clientCommands, "prefix"))
//            add("commands", Jval.newObject().apply {
//                Vars.netServer.clientCommands.commandList.forEach {
//                    add(it.text, it.paramText)
//                }
//            })
//            Call.clientPacketReliable(player.con, "commandList", this.toString())
//        }
//    }
}

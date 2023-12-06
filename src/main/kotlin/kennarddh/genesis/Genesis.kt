package kennarddh.genesis

import arc.util.Log
import arc.util.Reflect
import kennarddh.genesis.commands.InterceptedCommandHandler
import mindustry.mod.Plugin
import mindustry.server.ServerControl

@SuppressWarnings("unused")
class Genesis : Plugin() {
    override fun init() {
        Log.info("[Genesis] Loaded")

//        println(ServerControl.instance.handler.handleMessage("mods"))

        Reflect.set(ServerControl.instance, "handler", InterceptedCommandHandler("") { command, player ->
            println(command)
            println(player)
        })
//        println(ServerControl.instance.handler.handleMessage("mods"))
//        ServerControl.instance.handler = CustomCommandHandler("")

//        Vars.netServer.clientCommands
    }
}
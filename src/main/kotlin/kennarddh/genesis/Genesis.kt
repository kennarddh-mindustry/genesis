package kennarddh.genesis

import arc.util.CommandHandler
import arc.util.Log
import arc.util.Reflect
import mindustry.mod.Plugin
import mindustry.server.ServerControl

class CustomCommandHandler(prefix: String?) : CommandHandler(prefix) {
    override fun handleMessage(message: String?, params: Any?): CommandResponse {
        println("message: $message")
        println("params: $params")

        val result = super.handleMessage(message, params)

        println("result.command: ${result.command}")
        println("result.runCommand: ${result.runCommand}")
        println("result.type: ${result.type}")

        return result
    }
}

@SuppressWarnings("unused")
class Genesis : Plugin() {
    override fun init() {
        Log.info("[Genesis] Loaded")

//        println(ServerControl.instance.handler.handleMessage("mods"))

        Reflect.set(ServerControl.instance, "handler", CustomCommandHandler(""))
//        println(ServerControl.instance.handler.handleMessage("mods"))
//        ServerControl.instance.handler = CustomCommandHandler("")

//        Vars.netServer.clientCommands
    }
}
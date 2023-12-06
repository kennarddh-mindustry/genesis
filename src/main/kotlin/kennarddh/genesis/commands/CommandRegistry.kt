package kennarddh.genesis.commands

import arc.util.Reflect
import mindustry.Vars

class CommandRegistry {
    fun init() {
//        Reflect.set(ServerControl.instance, "handler", InterceptedCommandHandler("") { command, player ->
//            println("server")
//            println(command)
//            println(player)
//        })

        Reflect.set(Vars.netServer, "clientCommands", InterceptedCommandHandler("/") { command, player ->
            println("/client")
            println(command)
            println(player)
        })
    }
}
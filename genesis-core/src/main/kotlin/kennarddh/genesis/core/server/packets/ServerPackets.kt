package kennarddh.genesis.core.server.packets

import arc.func.Cons2
import arc.struct.ObjectMap
import arc.util.Reflect
import kennarddh.genesis.core.commons.MutablePriorityList
import kennarddh.genesis.core.commons.PriorityContainer
import kennarddh.genesis.core.commons.PriorityEnum
import mindustry.Vars
import mindustry.net.NetConnection
import kotlin.reflect.KClass

class ServerPackets {
    private val serverListeners: MutableMap<KClass<Any>, MutablePriorityList<(NetConnection, Any) -> Unit>> =
        mutableMapOf()

    fun init() {
        val originalServerListeners =
            Reflect.get<ObjectMap<Class<Any>, Cons2<NetConnection, Any>>>(Vars.net, "serverListeners")

        originalServerListeners.forEach {
            println(it)

            if (!serverListeners.contains(it.key.kotlin))
                serverListeners[it.key.kotlin] = MutablePriorityList()

            serverListeners[it.key.kotlin]!!.add(PriorityContainer(PriorityEnum.Normal) { connection, packet ->
                it.value.get(connection, packet)
            })
        }
    }
}
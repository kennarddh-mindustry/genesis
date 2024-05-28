package com.github.kennarddh.mindustry.genesis.core.filters

import arc.net.Server
import arc.net.Server.ServerConnectFilter
import arc.util.Reflect
import com.github.kennarddh.mindustry.genesis.core.commons.priority.MutablePriorityList
import com.github.kennarddh.mindustry.genesis.core.commons.priority.PriorityContainer
import com.github.kennarddh.mindustry.genesis.core.filters.annotations.Filter
import com.github.kennarddh.mindustry.genesis.core.filters.exceptions.InvalidFilterHandlerMethodException
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import kotlinx.coroutines.runBlocking
import mindustry.Vars
import mindustry.Vars.net
import mindustry.gen.Player
import mindustry.net.Administration.PlayerAction
import mindustry.net.ArcNetProvider
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

fun interface SuspendedChatFilter {
    suspend fun filter(player: Player, message: String): String?
}

fun interface SuspendedActionFilter {
    suspend fun allow(action: PlayerAction): Boolean
}

fun interface SuspendedServerConnectFilter {
    suspend fun accept(address: String): Boolean
}

class FiltersRegistry {
    private val chatFilters: MutablePriorityList<SuspendedChatFilter> = MutablePriorityList()
    private val actionFilters: MutablePriorityList<SuspendedActionFilter> = MutablePriorityList()
    private val connectFilters: MutablePriorityList<SuspendedServerConnectFilter> = MutablePriorityList()

    internal fun init() {
        val provider = Reflect.get<ArcNetProvider>(net, "provider")
        val server = Reflect.get<Server>(provider, "server")

        val prevConnectFilter = Reflect.get<ServerConnectFilter>(server, "connectFilter")

        Vars.netServer.admins.addChatFilter { player, message ->
            runBlocking {
                var output = message

                chatFilters.forEachPrioritized {
                    output = it.filter(player, output)
                }

                return@runBlocking output
            }
        }

        Vars.netServer.admins.addActionFilter { action ->
            runBlocking {
                actionFilters.forEachPrioritized {
                    if (!it.allow(action))
                        return@runBlocking false
                }

                return@runBlocking true
            }
        }

        server.setConnectFilter { address ->
            runBlocking {
                connectFilters.forEachPrioritized {
                    if (!it.accept(address)) {
                        return@runBlocking prevConnectFilter?.accept(address) ?: false
                    }
                }

                return@runBlocking true
            }
        }
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val serverPacketHandlerAnnotation = function.findAnnotation<Filter>() ?: continue

            val priority = serverPacketHandlerAnnotation.priority
            val type = serverPacketHandlerAnnotation.type

            val functionParameters = function.parameters.drop(1)

            when (type) {
                FilterType.Chat -> {
                    if (functionParameters.size != 2)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept exactly two parameters player and message string")

                    if (functionParameters[0].type.classifier != Player::class)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept player as the first parameter")

                    if (functionParameters[1].type.classifier != String::class)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept message string as the second parameter")

                    if (function.returnType.classifier != String::class)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return string")

                    val filter = SuspendedChatFilter { player, message ->
                        function.callSuspend(handler, player, message) as String?
                    }

                    chatFilters.add(PriorityContainer(priority, filter))
                }

                FilterType.Action -> {
                    if (functionParameters.size != 1)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept exactly one parameter PlayerAction")

                    if (functionParameters[0].type.classifier != PlayerAction::class)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept PlayerAction as the first parameter")

                    if (function.returnType.classifier != Boolean::class)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return boolean")

                    val filter = SuspendedActionFilter { action ->
                        function.callSuspend(handler, action) as Boolean
                    }

                    actionFilters.add(PriorityContainer(priority, filter))
                }

                FilterType.Connect -> {
                    if (functionParameters.size != 1)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept exactly one parameter address string")

                    if (functionParameters[0].type.classifier != String::class)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept address string as the first parameter")

                    if (function.returnType.classifier != Boolean::class)
                        throw InvalidFilterHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return boolean")

                    val filter = SuspendedServerConnectFilter { address ->
                        function.callSuspend(handler, address) as Boolean
                    }

                    connectFilters.add(PriorityContainer(priority, filter))
                }
            }
        }
    }
}
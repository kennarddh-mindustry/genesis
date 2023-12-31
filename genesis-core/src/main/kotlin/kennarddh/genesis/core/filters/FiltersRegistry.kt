package kennarddh.genesis.core.filters

import arc.net.Server
import arc.net.Server.ServerConnectFilter
import arc.util.Reflect
import kennarddh.genesis.core.commons.priority.MutablePriorityList
import kennarddh.genesis.core.commons.priority.PriorityContainer
import kennarddh.genesis.core.filters.annotations.Filter
import kennarddh.genesis.core.filters.exceptions.InvalidFilterHandlerMethodException
import kennarddh.genesis.core.handlers.Handler
import mindustry.Vars
import mindustry.Vars.net
import mindustry.gen.Player
import mindustry.net.Administration.*
import mindustry.net.ArcNetProvider
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible


class FiltersRegistry {
    private val chatFilters: MutablePriorityList<ChatFilter> = MutablePriorityList()
    private val actionFilters: MutablePriorityList<ActionFilter> = MutablePriorityList()
    private val connectFilters: MutablePriorityList<ServerConnectFilter> = MutablePriorityList()

    internal fun init() {
        val provider = Reflect.get<ArcNetProvider>(net, "provider")
        val server = Reflect.get<Server>(provider, "server")

        val prevConnectFilter = Reflect.get<ServerConnectFilter>(server, "connectFilter")

        Vars.netServer.admins.addChatFilter { player, message ->
            var output = message

            chatFilters.forEachPrioritized {
                output = it.filter(player, output)
            }

            return@addChatFilter output
        }

        Vars.netServer.admins.addActionFilter { action ->
            var output = true

            actionFilters.forEachPrioritized {
                if (!output)
                    return@forEachPrioritized

                output = it.allow(action)
            }

            return@addActionFilter output
        }

        server.setConnectFilter { address ->
            var output = true

            connectFilters.forEachPrioritized {
                if (!output)
                    return@forEachPrioritized

                output = it.accept(address)
            }

            if (output)
                return@setConnectFilter true

            prevConnectFilter?.accept(address) ?: false
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

                    val filter = ChatFilter { player, message ->
                        function.call(handler, player, message) as String
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

                    val filter = ActionFilter { action ->
                        function.call(handler, action) as Boolean
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

                    val filter = ServerConnectFilter { address ->
                        function.call(handler, address) as Boolean
                    }

                    connectFilters.add(PriorityContainer(priority, filter))
                }
            }
        }
    }
}
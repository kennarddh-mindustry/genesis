package kennarddh.genesis.core.server.packets

import arc.Events
import arc.func.Cons2
import arc.struct.ObjectMap
import arc.util.Log
import arc.util.Reflect
import arc.util.Strings
import arc.util.Time
import arc.util.io.ReusableByteOutStream
import arc.util.io.Writes
import kennarddh.genesis.core.commons.priority.MutablePriorityList
import kennarddh.genesis.core.commons.priority.PriorityContainer
import kennarddh.genesis.core.commons.priority.PriorityEnum
import kennarddh.genesis.core.events.exceptions.InvalidEventHandlerMethodException
import kennarddh.genesis.core.handlers.Handler
import kennarddh.genesis.core.server.packets.annotations.ServerPacketHandler
import kennarddh.genesis.core.server.packets.events.PlayerJoinConstruct
import mindustry.Vars
import mindustry.core.NetServer
import mindustry.core.Version
import mindustry.game.EventType
import mindustry.game.EventType.ConnectPacketEvent
import mindustry.game.EventType.PlayerConnect
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.net.NetConnection
import mindustry.net.Packets.*
import java.io.DataOutputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible


class ServerPacketsRegistry {
    private val serverListeners: MutableMap<KClass<Any>, MutablePriorityList<(NetConnection, Any) -> Boolean>> =
        mutableMapOf()

    private val writeBuffer = ReusableByteOutStream(127)
    private val outputBuffer = Writes(DataOutputStream(writeBuffer))

    internal fun init() {
        val originalServerListeners =
            Reflect.get<ObjectMap<Class<Any>, Cons2<NetConnection, Any>>>(Vars.net, "serverListeners")

        @Suppress("UNCHECKED_CAST")
        originalServerListeners.get(ConnectPacket::class.java as Class<Any>)

        addServerListener(Connect::class, PriorityEnum.Normal) { connection, _ ->
            Events.fire(EventType.ConnectionEvent(connection))

            true
        }

        addServerListener(Disconnect::class, PriorityEnum.Normal) { connection, packet ->
            if (connection.player != null) {
                NetServer.onDisconnect(connection.player, packet.reason)

                return@addServerListener false
            }

            true
        }

        addServerListener(ConnectPacket::class, PriorityEnum.Normal) { connection, packet ->
            if (connection.address.startsWith("steam:")) {
                packet.uuid = connection.address.substring("steam:".length)
            }

            Events.fire(ConnectPacketEvent(connection, packet))

            connection.connectTime = Time.millis()

            val uuid = packet.uuid

            if (connection.hasBegunConnecting) {
                connection.kick(KickReason.idInUse)

                return@addServerListener false
            }

            connection.hasBegunConnecting = true
            connection.mobile = packet.mobile

            if (packet.uuid == null || packet.usid == null) {
                connection.kick(KickReason.idInUse)

                return@addServerListener false
            }

            if (Vars.netServer.admins.playerLimit > 0 &&
                Groups.player.size() >= Vars.netServer.admins.playerLimit &&
                !Vars.netServer.admins.isAdmin(uuid, packet.usid)
            ) {
                connection.kick(KickReason.playerLimit)

                return@addServerListener false
            }

            val extraMods = packet.mods.copy()
            val missingMods = Vars.mods.getIncompatibility(extraMods)

            if (!extraMods.isEmpty || !missingMods.isEmpty) {
                //can't easily be localized since kick reasons can't have formatted text with them
                val result = StringBuilder("[accent]Incompatible mods![]\n\n")

                if (!missingMods.isEmpty) {
                    result.append("Missing:[lightgray]\n").append("> ").append(missingMods.toString("\n> "))
                    result.append("[]\n")
                }

                if (!extraMods.isEmpty)
                    result.append("Unnecessary mods:[lightgray]\n").append("> ").append(extraMods.toString("\n> "))

                connection.kick(result.toString(), 0)

                return@addServerListener false
            }

            if (packet.versionType == null || (packet.version == -1 || packet.versionType != Version.type) && Version.build != -1 && !Vars.netServer.admins.allowsCustomClients()) {
                connection.kick(if (Version.type != packet.versionType) KickReason.typeMismatch else KickReason.customClient)

                return@addServerListener false
            }

            val preventDuplicates = Vars.headless && Vars.netServer.admins.isStrict

            if (preventDuplicates) {
                if (
                    Groups.player.contains { player ->
                        Strings.stripColors(player.name).trim { it <= ' ' }
                            .equals(Strings.stripColors(packet.name).trim { it <= ' ' }, ignoreCase = true)
                    }
                ) {
                    connection.kick(KickReason.nameInUse)

                    return@addServerListener false
                }

                if (Groups.player.contains { player: Player -> player.uuid() == packet.uuid || player.usid() == packet.usid }) {
                    connection.uuid = packet.uuid

                    connection.kick(KickReason.idInUse)

                    return@addServerListener false
                }

                for (otherCon in Vars.net.connections) {
                    if (otherCon !== connection && uuid == otherCon.uuid) {
                        connection.uuid = packet.uuid

                        connection.kick(KickReason.idInUse)

                        return@addServerListener false
                    }
                }
            }

            packet.name = Vars.netServer.fixName(packet.name)

            if (packet.name.trim { it <= ' ' }.isEmpty()) {
                connection.kick(KickReason.nameEmpty)

                return@addServerListener false
            }

            if (packet.locale == null)
                packet.locale = "en"

            val ip: String = connection.address

            Vars.netServer.admins.updatePlayerJoined(uuid, ip, packet.name)

            if (packet.version != Version.build && Version.build != -1 && packet.version != -1) {
                connection.kick(if (packet.version > Version.build) KickReason.serverOutdated else KickReason.clientOutdated)

                return@addServerListener false
            }

            if (packet.version == -1) {
                connection.modclient = true
            }

            val player = Player.create()

            player.con = connection
            player.con.usid = packet.usid
            player.con.uuid = uuid
            player.con.mobile = packet.mobile
            player.name = packet.name
            player.locale = packet.locale
            player.color.set(packet.color).a(1f)

            Events.fire(PlayerJoinConstruct(player))

            try {
                writeBuffer.reset()

                player.write(outputBuffer)
            } catch (t: Throwable) {
                connection.kick(KickReason.nameEmpty)
                Log.err(t)

                return@addServerListener false
            }

            connection.player = player

            // Playing in pvp mode automatically assigns players to teams
            player.team(Vars.netServer.assignTeam(player))

            Vars.netServer.sendWorldData(player)

            Vars.platform.updateRPC()

            Events.fire(PlayerConnect(player))

            return@addServerListener true
        }
    }

    private fun <T : Any> addServerListener(
        packetType: KClass<T>,
        priority: PriorityEnum,
        handler: (NetConnection, T) -> Boolean
    ) {
        @Suppress("UNCHECKED_CAST")
        if (!serverListeners.contains(packetType as KClass<Any>)) {
            serverListeners[packetType] = MutablePriorityList()

            Vars.net.handleServer(packetType.java) { connection, packet ->
                serverListeners[packetType]!!.forEachPrioritized {
                    it(connection, packet)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        serverListeners[packetType]!!.add(PriorityContainer(priority, handler as (NetConnection, Any) -> Boolean))
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val serverPacketHandlerAnnotation = function.findAnnotation<ServerPacketHandler>() ?: continue

            val priority = serverPacketHandlerAnnotation.priority

            val functionParameters = function.parameters.drop(1)

            if (functionParameters.size != 2)
                throw InvalidEventHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept exactly two parameters connection and the packet type")

            if (functionParameters[0].type.classifier != NetConnection::class)
                throw InvalidEventHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept connection as the first parameter")

            if (function.returnType.classifier != Boolean::class)
                throw InvalidEventHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return boolean")

            val packetType = functionParameters[1].type.classifier as KClass<*>

            serverListeners[packetType]!!.add(PriorityContainer(priority) { connection, packet ->
                function.call(handler, connection, packet) as Boolean
            })
        }
    }
}
package kennarddh.genesis.commands

import kennarddh.genesis.handlers.Handler
import java.lang.reflect.Method

data class CommandData(val isServerSide: Boolean, val isClientSide: Boolean, val handler: Handler, val method: Method)

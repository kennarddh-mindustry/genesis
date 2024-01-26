package kennarddh.genesis.core.logging

import org.slf4j.ILoggerFactory
import org.slf4j.Logger


class ArcLoggerFactory : ILoggerFactory {
    private var loggers: MutableMap<String, ArcLogger> = mutableMapOf()

    /**
     * Return an appropriate [ArcLogger] instance by name.
     *
     * This method will call [.createLogger] if the logger
     * has not been created yet.
     */
    override fun getLogger(name: String): Logger = loggers.computeIfAbsent(name, ::createLogger)

    /**
     * Actually creates the logger for the given name.
     */
    private fun createLogger(name: String?): ArcLogger {
        return ArcLogger(name)
    }
}
package com.nekgamebling

import com.nekgamebling.config.DatabaseConfig
import com.nekgamebling.config.gameCoreModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Core plugin for the IGambling application.
 * Initializes database and dependency injection.
 */
val CorePlugin = createApplicationPlugin(name = "CorePlugin") {
    // Initialize database
    DatabaseConfig.init()

    // Initialize Koin if not already installed
    if (application.pluginOrNull(Koin) == null) {
        application.install(Koin) {
            slf4jLogger()
            modules(application.gameCoreModule)
        }
    }
}

/**
 * Extension function to install the core plugin.
 */
fun Application.installCore() {
    install(CorePlugin)
}

package com.nekgamebling.infrastructure.handler.spin

import application.port.inbound.SpinSettledEventHandler
import org.koin.dsl.module

/**
 * Koin module for SpinSettledEvent handlers.
 * Registers all handlers that process spin settlement events.
 */
internal val SpinSettledHandlerModule = module {
    factory<List<SpinSettledEventHandler>> {
        listOf(
            RecordGameWinHandler(get())
        )
    }
}
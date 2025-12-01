package com.nekgamebling.infrastructure.handler.spin

import application.port.inbound.SpinSettleEventHandler
import org.koin.dsl.module

internal val SpinSettleEventHandlers = module {
    factory<List<SpinSettleEventHandler>> {
        listOf(AddGameWonHandler(get()))
    }
}
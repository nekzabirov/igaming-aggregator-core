package com.nekgamebling.infrastructure.handler

import com.nekgamebling.infrastructure.handler.spin.SpinSettledHandlerModule
import org.koin.dsl.module

/**
 * Aggregates all event handler modules.
 */
val HandlerModule = module {
    includes(SpinSettledHandlerModule)
}
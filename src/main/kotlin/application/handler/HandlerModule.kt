package com.nekgamebling.application.handler

import com.nekgamebling.application.handler.spin.SpinSettledHandlerModule
import org.koin.dsl.module

/**
 * Aggregates all event handler modules.
 */
val HandlerModule = module {
    includes(SpinSettledHandlerModule)
}
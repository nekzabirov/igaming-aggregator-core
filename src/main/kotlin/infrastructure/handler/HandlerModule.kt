package com.nekgamebling.infrastructure.handler

import com.nekgamebling.infrastructure.handler.spin.SpinSettleEventHandlers
import org.koin.dsl.module

val HandlerModule = module {
    includes(SpinSettleEventHandlers)
}
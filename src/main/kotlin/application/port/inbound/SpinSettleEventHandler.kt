package application.port.inbound

import application.event.SpinSettledEvent

interface SpinSettleEventHandler {
    suspend fun handle(event: SpinSettledEvent)
}
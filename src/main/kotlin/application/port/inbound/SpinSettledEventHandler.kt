package application.port.inbound

import application.event.SpinSettledEvent

/**
 * Handler interface for processing SpinSettledEvent.
 * Implementations react to spin settlement (win/loss) outcomes.
 */
interface SpinSettledEventHandler {
    suspend fun handle(event: SpinSettledEvent)
}
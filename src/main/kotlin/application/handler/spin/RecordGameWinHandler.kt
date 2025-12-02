package com.nekgamebling.application.handler.spin

import application.event.SpinSettledEvent
import application.port.inbound.SpinSettledEventHandler
import application.usecase.game.AddGameWinUsecase
import com.nekgamebling.shared.Logger

/**
 * Handles SpinSettledEvent by recording game wins.
 * Only processes regular spins (not free spins).
 */
class RecordGameWinHandler(
    private val addGameWinUsecase: AddGameWinUsecase
) : SpinSettledEventHandler {

    override suspend fun handle(event: SpinSettledEvent) {
        if (event.freeSpinId != null) {
            return
        }

        Logger.info("Recording game win for player ${event.playerId} on ${event.gameIdentity}")

        addGameWinUsecase(
            gameIdentity = event.gameIdentity,
            playerId = event.playerId,
            amount = event.amount,
            currency = event.currency
        )
    }
}
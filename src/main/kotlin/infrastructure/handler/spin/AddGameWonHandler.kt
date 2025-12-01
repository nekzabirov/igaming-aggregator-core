package com.nekgamebling.infrastructure.handler.spin

import application.event.SpinSettledEvent
import application.port.inbound.SpinSettleEventHandler
import application.usecase.game.AddGameWonUsecase
import com.nekgamebling.shared.Logger

class AddGameWonHandler(private val gameWonUsecase: AddGameWonUsecase) : SpinSettleEventHandler {
    override suspend fun handle(event: SpinSettledEvent) {
        Logger.info("Handling AddGameWonHandler: $event")

        if (event.freeSpinId != null) return

        gameWonUsecase(
            gameIdentity = event.gameIdentity,
            playerId = event.playerId,
            amount = event.amount,
            currency = event.currency
        )
    }
}
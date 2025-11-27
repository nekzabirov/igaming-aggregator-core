package com.nekgamebling.application.usecase.spin

import com.nekgamebling.application.event.SpinPlacedEvent
import com.nekgamebling.application.port.outbound.EventPublisherPort
import com.nekgamebling.application.service.GameService
import com.nekgamebling.application.service.SessionService
import com.nekgamebling.application.service.SpinCommand
import com.nekgamebling.application.service.SpinService
import com.nekgamebling.domain.common.error.GameUnavailableError
import com.nekgamebling.shared.value.SessionToken

/**
 * Use case for placing a spin (making a bet).
 */
class PlaceSpinUsecase(
    private val sessionService: SessionService,
    private val gameService: GameService,
    private val spinService: SpinService,
    private val eventPublisher: EventPublisherPort
) {
    suspend operator fun invoke(
        token: SessionToken,
        gameSymbol: String,
        extRoundId: String,
        transactionId: String,
        freeSpinId: String?,
        amount: Int
    ): Result<Unit> {
        // Find session
        val session = sessionService.findByToken(token.value).getOrElse {
            return Result.failure(it)
        }

        // Find game
        val game = gameService.findBySymbol(gameSymbol).getOrElse {
            return Result.failure(it)
        }

        // Validate game is active
        if (!game.isPlayable()) {
            return Result.failure(GameUnavailableError(gameSymbol))
        }

        // Create spin command
        val command = SpinCommand(
            extRoundId = extRoundId,
            transactionId = transactionId,
            amount = amount,
            freeSpinId = freeSpinId
        )

        // Place spin
        spinService.place(session, game, command).getOrElse {
            return Result.failure(it)
        }

        // Publish event
        eventPublisher.publish(
            SpinPlacedEvent(
                gameId = game.id.toString(),
                gameIdentity = game.identity,
                amount = amount,
                currency = session.currency,
                playerId = session.playerId,
                freeSpinId = freeSpinId
            )
        )

        return Result.success(Unit)
    }
}

package application.usecase.spin

import application.event.SpinPlacedEvent
import application.port.outbound.EventPublisherPort
import application.service.GameService
import application.service.SessionService
import application.service.SpinCommand
import application.service.SpinService
import domain.common.error.GameUnavailableError
import shared.value.SessionToken

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
        val session = sessionService.findByToken(token).getOrElse {
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

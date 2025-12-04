package application.usecase.spin

import application.event.SpinSettledEvent
import application.port.outbound.EventPublisherAdapter
import application.service.GameService
import application.service.SpinCommand
import application.service.SpinService
import domain.session.model.Session
import java.math.BigInteger

/**
 * Use case for settling a spin (recording win/loss).
 * Accepts pre-resolved Session to avoid duplicate lookups.
 */
class SettleSpinUsecase(
    private val spinService: SpinService,
    private val gameService: GameService,
    private val eventPublisher: EventPublisherAdapter
) {
    suspend operator fun invoke(
        session: Session,
        extRoundId: String,
        transactionId: String,
        freeSpinId: String?,
        winAmount: BigInteger,
        finishRound: Boolean = false
    ): Result<Unit> {
        val command = SpinCommand(
            extRoundId = extRoundId,
            transactionId = transactionId,
            amount = winAmount,
            freeSpinId = freeSpinId
        )

        spinService.settle(session, extRoundId, command).getOrElse {
            return Result.failure(it)
        }

        if (finishRound) {
            spinService.closeRound(session, extRoundId).getOrElse {
                return Result.failure(it)
            }
        }

        val game = gameService.findById(session.gameId).getOrElse {
            return Result.failure(it)
        }

        eventPublisher.publish(
            SpinSettledEvent(
                gameIdentity = game.identity,
                amount = winAmount,
                currency = session.currency,
                playerId = session.playerId,
                freeSpinId = freeSpinId,
            )
        )

        return Result.success(Unit)
    }
}

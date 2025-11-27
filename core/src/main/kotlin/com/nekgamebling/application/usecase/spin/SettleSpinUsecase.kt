package com.nekgamebling.application.usecase.spin

import com.nekgamebling.application.event.SpinSettledEvent
import com.nekgamebling.application.port.outbound.EventPublisherPort
import com.nekgamebling.application.service.SessionService
import com.nekgamebling.application.service.SpinCommand
import com.nekgamebling.application.service.SpinService
import com.nekgamebling.shared.value.SessionToken

/**
 * Use case for settling a spin (recording win/loss).
 */
class SettleSpinUsecase(
    private val sessionService: SessionService,
    private val spinService: SpinService,
    private val eventPublisher: EventPublisherPort
) {
    suspend operator fun invoke(
        token: SessionToken,
        extRoundId: String,
        transactionId: String,
        freeSpinId: String?,
        winAmount: Int
    ): Result<Unit> {
        // Find session
        val session = sessionService.findByToken(token.value).getOrElse {
            return Result.failure(it)
        }

        // Create settle command
        val command = SpinCommand(
            extRoundId = extRoundId,
            transactionId = transactionId,
            amount = winAmount,
            freeSpinId = freeSpinId
        )

        // Settle spin
        spinService.settle(session, extRoundId, command).getOrElse {
            return Result.failure(it)
        }

        // Publish event
        eventPublisher.publish(
            SpinSettledEvent(
                gameId = session.gameId.toString(),
                gameIdentity = "", // Would need game lookup for identity
                amount = winAmount,
                currency = session.currency,
                playerId = session.playerId,
                freeSpinId = freeSpinId,
                winAmount = winAmount
            )
        )

        return Result.success(Unit)
    }
}

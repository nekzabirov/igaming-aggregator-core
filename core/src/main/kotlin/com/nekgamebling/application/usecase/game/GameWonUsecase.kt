package com.nekgamebling.application.usecase.game

import com.nekgamebling.application.event.GameWonEvent
import com.nekgamebling.application.port.outbound.EventPublisherPort
import com.nekgamebling.domain.common.error.NotFoundError
import com.nekgamebling.domain.game.repository.GameRepository
import com.nekgamebling.domain.game.repository.GameWonRepository
import com.nekgamebling.shared.value.Currency

/**
 * Use case for recording a game win.
 */
class AddGameWonUsecase(
    private val gameRepository: GameRepository,
    private val gameWonRepository: GameWonRepository,
    private val eventPublisher: EventPublisherPort
) {
    suspend operator fun invoke(
        gameIdentity: String,
        playerId: String,
        amount: Int,
        currency: Currency
    ): Result<Unit> {
        val game = gameRepository.findByIdentity(gameIdentity)
            ?: return Result.failure(NotFoundError("Game", gameIdentity))

        gameWonRepository.save(game.id, playerId, amount, currency.value)

        eventPublisher.publish(
            GameWonEvent(
                gameId = game.id.toString(),
                gameIdentity = game.identity,
                playerId = playerId,
                amount = amount,
                currency = currency
            )
        )

        return Result.success(Unit)
    }
}

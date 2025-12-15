package application.usecase.game

import domain.game.model.GameWithDetails
import domain.game.repository.GameRepository

/**
 * Use case for finding a game by identity with all related details.
 */
class FindGameUsecase(
    private val gameRepository: GameRepository
) {
    /**
     * Find a game by identity and return full details including provider and aggregator info.
     *
     * @param identity The unique identity of the game
     * @return Result containing GameWithDetails if found, or failure if not found
     */
    suspend operator fun invoke(identity: String): Result<GameWithDetails> {
        val game = gameRepository.findWithDetailsByIdentity(identity)
            ?: return Result.failure(GameNotFoundException(identity))

        return Result.success(game)
    }
}

class GameNotFoundException(identity: String) : Exception("Game not found: $identity")

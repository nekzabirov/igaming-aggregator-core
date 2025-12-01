package domain.game.repository

import domain.collection.model.Collection
import domain.game.model.Game
import domain.game.model.GameVariant
import domain.provider.model.Provider

/**
 * Game list item containing game with variant, provider and collections.
 */
data class GameListItem(
    val game: Game,
    val variant: GameVariant,
    val provider: Provider,
    val collections: List<Collection> = emptyList()
)

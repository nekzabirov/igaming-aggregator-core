package domain.game.model

import domain.provider.model.Provider

data class GameVariantWithDetail(
    val variant: GameVariant,

    val game: Game? = null,

    val provider: Provider? = null
)

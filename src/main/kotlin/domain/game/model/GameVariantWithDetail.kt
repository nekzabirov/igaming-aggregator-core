package com.nekgamebling.domain.game.model

import com.nekgamebling.domain.provider.model.Provider

data class GameVariantWithDetail(
    val variant: GameVariant,

    val game: Game? = null,

    val provider: Provider? = null
)

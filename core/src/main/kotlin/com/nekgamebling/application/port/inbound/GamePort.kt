package com.nekgamebling.application.port.inbound

import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.domain.game.model.GameVariant
import com.nekgamebling.domain.game.model.GameVariantWithDetail
import com.nekgamebling.shared.value.Aggregator
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable

interface GamePort {
    suspend fun syncGame(variants: List<GameVariant>, aggregatorInfo: AggregatorInfo)

    suspend fun findVariantsAll(
        query: String,
        aggregator: Aggregator?,
        gameIdentity: String?,
        pageable: Pageable
    ): Page<GameVariantWithDetail>
}
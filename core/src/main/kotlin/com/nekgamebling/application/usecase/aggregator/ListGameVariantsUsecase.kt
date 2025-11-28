package com.nekgamebling.application.usecase.aggregator

import com.nekgamebling.application.port.inbound.GamePort
import com.nekgamebling.domain.game.model.GameVariant
import com.nekgamebling.domain.game.model.GameVariantWithDetail
import com.nekgamebling.domain.game.repository.GameRepository
import com.nekgamebling.domain.game.repository.GameVariantRepository
import com.nekgamebling.domain.provider.model.Provider
import com.nekgamebling.shared.value.Aggregator
import com.nekgamebling.shared.value.ImageMap
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable

/**
 * Filter for game variants.
 */
data class GameVariantFilter(
    val query: String = "",
    val aggregator: Aggregator? = null,
    val gameIdentity: String? = null
) {
    class Builder {
        private var query: String = ""
        private var aggregator: Aggregator? = null
        private var gameIdentity: String? = null

        fun withQuery(query: String) = apply { this.query = query }
        fun withAggregator(aggregator: Aggregator?) = apply { this.aggregator = aggregator }
        fun withGameIdentity(gameIdentity: String?) = apply { this.gameIdentity = gameIdentity }

        fun build() = GameVariantFilter(query, aggregator, gameIdentity)
    }
}

/**
 * Use case for listing game variants.
 */
class ListGameVariantsUsecase(
    private val gameVariantRepository: GameVariantRepository,
    private val gameRepository: GameRepository,
    private val gamePort: GamePort
) {
    suspend operator fun invoke(
        pageable: Pageable,
        filterBuilder: GameVariantFilter.Builder.() -> Unit = {}
    ): Page<GameVariantWithDetail> {
        val filter = GameVariantFilter.Builder().apply(filterBuilder).build()

        return gamePort.findVariantsAll(
            query = filter.query,
            aggregator = filter.aggregator,
            gameIdentity = filter.gameIdentity,
            pageable = pageable
        )
    }
}

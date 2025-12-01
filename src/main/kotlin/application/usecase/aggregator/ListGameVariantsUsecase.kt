package com.nekgamebling.application.usecase.aggregator

import com.nekgamebling.domain.game.model.GameVariantWithDetail
import com.nekgamebling.domain.game.repository.GameVariantFilter
import com.nekgamebling.domain.game.repository.GameVariantRepository
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable

/**
 * Use case for listing game variants with details.
 */
class ListGameVariantsUsecase(
    private val gameVariantRepository: GameVariantRepository
) {
    suspend operator fun invoke(
        pageable: Pageable,
        filterBuilder: GameVariantFilter.Builder.() -> Unit = {}
    ): Page<GameVariantWithDetail> {
        val filter = GameVariantFilter.Builder().apply(filterBuilder).build()
        return gameVariantRepository.findAllWithDetails(filter, pageable)
    }
}

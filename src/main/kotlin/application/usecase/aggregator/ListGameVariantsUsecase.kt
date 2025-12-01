package application.usecase.aggregator

import domain.game.model.GameVariantWithDetail
import domain.game.repository.GameVariantFilter
import domain.game.repository.GameVariantRepository
import shared.value.Page
import shared.value.Pageable

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

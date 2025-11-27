package com.nekgamebling.application.usecase.game

import com.nekgamebling.domain.game.repository.GameFilter
import com.nekgamebling.domain.game.repository.GameListItem
import com.nekgamebling.domain.game.repository.GameRepository
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable

/**
 * Use case for listing games with filtering and pagination.
 * Uses constructor injection for dependencies.
 */
class ListGamesUsecase(
    private val gameRepository: GameRepository
) {
    /**
     * List games with filtering and pagination.
     */
    suspend operator fun invoke(
        pageable: Pageable,
        filter: GameFilter = GameFilter.EMPTY
    ): Page<GameListItem> {
        return gameRepository.findAll(pageable, filter)
    }

    /**
     * List games with DSL-style filter builder.
     */
    suspend operator fun invoke(
        pageable: Pageable,
        filterBuilder: GameFilter.Builder.() -> Unit
    ): Page<GameListItem> {
        val filter = GameFilter.builder().apply(filterBuilder).build()
        return invoke(pageable, filter)
    }
}

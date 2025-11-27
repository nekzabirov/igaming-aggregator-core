package com.nekgamebling.application.usecase.collection

import com.nekgamebling.domain.collection.model.Collection
import com.nekgamebling.domain.collection.repository.CollectionRepository
import com.nekgamebling.domain.common.error.DuplicateEntityError
import com.nekgamebling.domain.common.error.NotFoundError
import com.nekgamebling.domain.game.repository.GameRepository
import com.nekgamebling.shared.value.ImageMap
import com.nekgamebling.shared.value.LocaleName
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable
import java.util.UUID

/**
 * Use case for adding a new collection.
 */
class AddCollectionUsecase(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(identity: String, name: LocaleName, images: ImageMap = ImageMap.EMPTY): Result<Collection> {
        if (collectionRepository.existsByIdentity(identity)) {
            return Result.failure(DuplicateEntityError("Collection", identity))
        }

        val collection = Collection(
            id = UUID.randomUUID(),
            identity = identity,
            name = name,
            images = images
        )

        return try {
            val saved = collectionRepository.save(collection)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for updating a collection.
 */
class UpdateCollectionUsecase(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(
        identity: String,
        name: LocaleName? = null,
        order: Int? = null,
        active: Boolean? = null,
        images: ImageMap? = null
    ): Result<Collection> {
        val existing = collectionRepository.findByIdentity(identity)
            ?: return Result.failure(NotFoundError("Collection", identity))

        val updated = existing.copy(
            name = name ?: existing.name,
            order = order ?: existing.order,
            active = active ?: existing.active,
            images = images ?: existing.images
        )

        return try {
            val saved = collectionRepository.update(updated)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for adding a game to a collection.
 */
class AddGameCollectionUsecase(
    private val collectionRepository: CollectionRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(collectionIdentity: String, gameIdentity: String): Result<Unit> {
        val collection = collectionRepository.findByIdentity(collectionIdentity)
            ?: return Result.failure(NotFoundError("Collection", collectionIdentity))

        val game = gameRepository.findByIdentity(gameIdentity)
            ?: return Result.failure(NotFoundError("Game", gameIdentity))

        collectionRepository.addGame(collection.id, game.id)
        return Result.success(Unit)
    }
}

/**
 * Use case for removing a game from a collection.
 */
class RemoveGameCollectionUsecase(
    private val collectionRepository: CollectionRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(collectionIdentity: String, gameIdentity: String): Result<Unit> {
        val collection = collectionRepository.findByIdentity(collectionIdentity)
            ?: return Result.failure(NotFoundError("Collection", collectionIdentity))

        val game = gameRepository.findByIdentity(gameIdentity)
            ?: return Result.failure(NotFoundError("Game", gameIdentity))

        collectionRepository.removeGame(collection.id, game.id)
        return Result.success(Unit)
    }
}

/**
 * Use case for changing game order in a collection.
 */
class ChangeGameOrderUsecase(
    private val collectionRepository: CollectionRepository,
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(collectionIdentity: String, gameIdentity: String, order: Int): Result<Unit> {
        val collection = collectionRepository.findByIdentity(collectionIdentity)
            ?: return Result.failure(NotFoundError("Collection", collectionIdentity))

        val game = gameRepository.findByIdentity(gameIdentity)
            ?: return Result.failure(NotFoundError("Game", gameIdentity))

        collectionRepository.updateGameOrder(collection.id, game.id, order)
        return Result.success(Unit)
    }
}

/**
 * Collection list item with game counts.
 */
data class CollectionListItem(
    val category: Collection,
    val totalGamesCount: Int,
    val activeGamesCount: Int
)

/**
 * Filter for listing collections.
 */
data class CollectionFilter(
    val query: String = "",
    val active: Boolean? = null
) {
    class Builder {
        private var query: String = ""
        private var active: Boolean? = null

        fun withQuery(query: String) = apply { this.query = query }
        fun withActive(active: Boolean?) = apply { this.active = active }

        fun build() = CollectionFilter(query, active)
    }
}

/**
 * Use case for listing collections.
 */
class ListCollectionUsecase(
    private val collectionRepository: CollectionRepository
) {
    suspend operator fun invoke(
        pageable: Pageable,
        filterBuilder: (CollectionFilter.Builder) -> Unit = {}
    ): Page<CollectionListItem> {
        val filter = CollectionFilter.Builder().also(filterBuilder).build()

        val page = collectionRepository.findAll(pageable, filter.active ?: false)

        // TODO: Add game counts - for now return zeros
        val items = page.items.map { collection ->
            CollectionListItem(
                category = collection,
                totalGamesCount = 0,
                activeGamesCount = 0
            )
        }

        return Page(
            items = items,
            totalPages = page.totalPages,
            totalItems = page.totalItems,
            currentPage = page.currentPage
        )
    }
}

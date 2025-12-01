package com.nekgamebling.domain.collection.repository

import com.nekgamebling.domain.collection.model.Collection
import com.nekgamebling.domain.collection.model.CollectionGame
import com.nekgamebling.shared.value.Page
import com.nekgamebling.shared.value.Pageable
import java.util.UUID

/**
 * Repository interface for Collection entity operations.
 */
interface CollectionRepository {
    suspend fun findById(id: UUID): Collection?
    suspend fun findByIdentity(identity: String): Collection?
    suspend fun save(collection: Collection): Collection
    suspend fun update(collection: Collection): Collection
    suspend fun delete(id: UUID): Boolean
    suspend fun existsByIdentity(identity: String): Boolean
    suspend fun findAll(pageable: Pageable, activeOnly: Boolean = false): Page<Collection>

    /**
     * Game-collection relationship management.
     */
    suspend fun addGame(collectionId: UUID, gameId: UUID, order: Int = 0): Boolean
    suspend fun removeGame(collectionId: UUID, gameId: UUID): Boolean
    suspend fun updateGameOrder(collectionId: UUID, gameId: UUID, order: Int): Boolean
    suspend fun findGamesInCollection(collectionId: UUID): List<CollectionGame>
    suspend fun findCollectionsForGame(gameId: UUID): List<Collection>
}

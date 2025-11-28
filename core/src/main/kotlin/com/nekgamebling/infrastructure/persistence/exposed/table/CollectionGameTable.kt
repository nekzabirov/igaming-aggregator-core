package com.nekgamebling.infrastructure.persistence.exposed.table

import org.jetbrains.exposed.dao.id.UUIDTable

object CollectionGameTable : UUIDTable("collection_games") {
    val categoryId = reference("category_id", CollectionTable.id)
    val gameId = reference("game_id", GameTable.id)
    val order = integer("order").default(0)

    init {
        uniqueIndex(categoryId, gameId)
    }
}

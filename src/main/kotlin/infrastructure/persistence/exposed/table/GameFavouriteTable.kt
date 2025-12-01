package com.nekgamebling.infrastructure.persistence.exposed.table

import org.jetbrains.exposed.dao.id.UUIDTable

object GameFavouriteTable : UUIDTable("game_favourites") {
    val playerId = varchar("player_id", 100)
    val gameId = reference("game_id", GameTable.id)

    init {
        uniqueIndex(playerId, gameId)
    }
}

package com.nekgamebling.infrastructure.persistence.exposed.table

object GameWonTable : BaseTable("game_wons") {
    val gameId = reference("game_id", GameTable.id)
    val playerId = varchar("player_id", 100)
    val amount = integer("amount")
    val currency = varchar("currency", 3)
}

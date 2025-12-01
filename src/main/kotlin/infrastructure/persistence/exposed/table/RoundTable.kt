package com.nekgamebling.infrastructure.persistence.exposed.table

object RoundTable : BaseTable("rounds") {
    val sessionId = reference("session_id", SessionTable.id)
    val gameId = reference("game_id", GameTable.id)
    val extId = varchar("ext_id", 255)
    val finished = bool("finished").default(false)

    init {
        uniqueIndex(sessionId, extId)
    }
}

package com.nekgamebling.infrastructure.persistence.exposed.repository

import com.nekgamebling.domain.game.repository.GameWonRepository
import com.nekgamebling.infrastructure.persistence.exposed.table.GameWonTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

/**
 * Exposed implementation of GameWonRepository.
 */
class ExposedGameWonRepository : GameWonRepository {

    override suspend fun save(gameId: UUID, playerId: String, amount: Int, currency: String): Boolean =
        newSuspendedTransaction {
            GameWonTable.insert {
                it[GameWonTable.gameId] = gameId
                it[GameWonTable.playerId] = playerId
                it[GameWonTable.amount] = amount
                it[GameWonTable.currency] = currency
            }
            true
        }
}

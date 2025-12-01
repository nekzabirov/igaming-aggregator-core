package infrastructure.persistence.exposed.repository

import domain.game.repository.GameFavouriteRepository
import infrastructure.persistence.exposed.table.GameFavouriteTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

/**
 * Exposed implementation of GameFavouriteRepository.
 */
class ExposedGameFavouriteRepository : GameFavouriteRepository {

    override suspend fun add(playerId: String, gameId: UUID): Boolean = newSuspendedTransaction {
        if (exists(playerId, gameId)) return@newSuspendedTransaction true

        GameFavouriteTable.insert {
            it[GameFavouriteTable.playerId] = playerId
            it[GameFavouriteTable.gameId] = gameId
        }
        true
    }

    override suspend fun remove(playerId: String, gameId: UUID): Boolean = newSuspendedTransaction {
        GameFavouriteTable.deleteWhere {
            (GameFavouriteTable.playerId eq playerId) and (GameFavouriteTable.gameId eq gameId)
        } > 0
    }

    override suspend fun exists(playerId: String, gameId: UUID): Boolean = newSuspendedTransaction {
        GameFavouriteTable.selectAll()
            .where { (GameFavouriteTable.playerId eq playerId) and (GameFavouriteTable.gameId eq gameId) }
            .count() > 0
    }

    override suspend fun findByPlayer(playerId: String): List<UUID> = newSuspendedTransaction {
        GameFavouriteTable.selectAll()
            .where { GameFavouriteTable.playerId eq playerId }
            .map { it[GameFavouriteTable.gameId].value }
    }
}

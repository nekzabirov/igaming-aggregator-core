package com.nekgamebling.domain.session.repository

import com.nekgamebling.domain.session.model.Round
import com.nekgamebling.domain.session.model.Session
import com.nekgamebling.domain.session.model.Spin
import com.nekgamebling.shared.value.SpinType
import java.util.UUID

/**
 * Repository interface for Session entity operations.
 */
interface SessionRepository {
    suspend fun findById(id: UUID): Session?
    suspend fun findByToken(token: String): Session?
    suspend fun save(session: Session): Session
    suspend fun delete(id: UUID): Boolean
}

/**
 * Repository interface for Round entity operations.
 */
interface RoundRepository {
    suspend fun findById(id: UUID): Round?
    suspend fun findByExtId(sessionId: UUID, extId: String): Round?
    suspend fun findBySessionId(sessionId: UUID): List<Round>
    suspend fun save(round: Round): Round
    suspend fun update(round: Round): Round
    suspend fun finish(id: UUID): Boolean

    /**
     * Find or create a round for the given session and external ID.
     */
    suspend fun findOrCreate(sessionId: UUID, gameId: UUID, extId: String): Round
}

/**
 * Repository interface for Spin entity operations.
 */
interface SpinRepository {
    suspend fun findById(id: UUID): Spin?
    suspend fun findByRoundId(roundId: UUID): List<Spin>
    suspend fun findByRoundIdAndType(roundId: UUID, type: SpinType): Spin?
    suspend fun findByExtId(extId: String): Spin?
    suspend fun save(spin: Spin): Spin

    /**
     * Find place spin for a round.
     */
    suspend fun findPlaceSpinByRoundId(roundId: UUID): Spin?
}

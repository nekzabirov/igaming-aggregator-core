package com.nekgamebling.application.service

import com.nekgamebling.domain.common.error.NotFoundError
import com.nekgamebling.domain.common.error.SessionInvalidError
import com.nekgamebling.domain.session.model.Session
import com.nekgamebling.domain.session.repository.SessionRepository
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

/**
 * Application service for session-related operations.
 * Uses constructor injection for all dependencies.
 */
class SessionService(
    private val sessionRepository: SessionRepository
) {
    private val secureRandom = SecureRandom()

    /**
     * Generate a secure session token.
     */
    fun generateSessionToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Find session by token.
     */
    suspend fun findByToken(token: String): Result<Session> {
        val session = sessionRepository.findByToken(token)
            ?: return Result.failure(SessionInvalidError(token))

        return Result.success(session)
    }

    /**
     * Find session by ID.
     */
    suspend fun findById(id: UUID): Result<Session> {
        val session = sessionRepository.findById(id)
            ?: return Result.failure(NotFoundError("Session", id.toString()))

        return Result.success(session)
    }

    /**
     * Create a new session.
     */
    suspend fun createSession(session: Session): Result<Session> {
        val savedSession = sessionRepository.save(session)
        return Result.success(savedSession)
    }
}

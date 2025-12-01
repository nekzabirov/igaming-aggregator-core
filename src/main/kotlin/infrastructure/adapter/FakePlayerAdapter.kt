package com.nekgamebling.infrastructure.adapter

import com.nekgamebling.application.port.outbound.PlayerPort

/**
 * Fake player adapter for development/testing.
 * Replace with real implementation in production.
 */
class FakePlayerAdapter : PlayerPort {
    private val betLimits = mutableMapOf<String, Int>()

    fun setBetLimit(playerId: String, limit: Int) {
        betLimits[playerId] = limit
    }

    override suspend fun findCurrentBetLimit(playerId: String): Result<Int?> {
        return Result.success(betLimits[playerId])
    }
}

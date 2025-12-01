package com.nekgamebling.application.port.outbound

import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.domain.game.model.GameVariant

/**
 * Outbound port for game synchronization.
 * Implementation handles transaction management for complex sync operations.
 */
interface GameSyncPort {
    suspend fun syncGame(variants: List<GameVariant>, aggregatorInfo: AggregatorInfo)
}

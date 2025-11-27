package com.nekgamebling.infrastructure.aggregator.onegamehub.adapter

import com.nekgamebling.application.port.outbound.AggregatorGameSyncPort
import com.nekgamebling.domain.aggregator.model.AggregatorGame
import com.nekgamebling.domain.aggregator.model.AggregatorInfo

/**
 * OneGameHub implementation for syncing games.
 */
class OneGameHubGameSyncAdapter(
    private val aggregatorInfo: AggregatorInfo
) : AggregatorGameSyncPort {

    override suspend fun listGames(): Result<List<AggregatorGame>> {
        // TODO: Implement actual API call to OneGameHub
        // GET /api/games/list
        // Headers: { "X-Operator-Id": "...", "X-Api-Key": "..." }
        //
        // Response:
        // {
        //   "games": [
        //     {
        //       "symbol": "...",
        //       "name": "...",
        //       "provider": "...",
        //       "features": {...}
        //     }
        //   ]
        // }

        // For now, return empty list - actual implementation should make HTTP call
        return Result.success(emptyList())
    }
}

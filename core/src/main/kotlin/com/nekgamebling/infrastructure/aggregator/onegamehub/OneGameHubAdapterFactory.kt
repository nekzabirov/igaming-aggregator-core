package com.nekgamebling.infrastructure.aggregator.onegamehub

import com.nekgamebling.application.port.outbound.AggregatorAdapterFactory
import com.nekgamebling.application.port.outbound.AggregatorFreespinPort
import com.nekgamebling.application.port.outbound.AggregatorGameSyncPort
import com.nekgamebling.application.port.outbound.AggregatorLaunchUrlPort
import com.nekgamebling.domain.aggregator.model.AggregatorInfo
import com.nekgamebling.shared.value.Aggregator

/**
 * Factory for creating OneGameHub aggregator adapters.
 */
class OneGameHubAdapterFactory : AggregatorAdapterFactory {

    override fun supports(aggregator: Aggregator): Boolean {
        return aggregator == Aggregator.ONEGAMEHUB
    }

    override fun createLaunchUrlAdapter(aggregatorInfo: AggregatorInfo): AggregatorLaunchUrlPort {
        return OneGameHubLaunchUrlAdapter(aggregatorInfo)
    }

    override fun createFreespinAdapter(aggregatorInfo: AggregatorInfo): AggregatorFreespinPort {
        return OneGameHubFreespinAdapter(aggregatorInfo)
    }

    override fun createGameSyncAdapter(aggregatorInfo: AggregatorInfo): AggregatorGameSyncPort {
        return OneGameHubGameSyncAdapter(aggregatorInfo)
    }
}

package infrastructure.aggregator

import application.port.outbound.AggregatorAdapterFactory
import application.port.outbound.AggregatorAdapterRegistry
import infrastructure.aggregator.onegamehub.OneGameHubAdapterFactory
import shared.value.Aggregator

/**
 * Default implementation of AggregatorAdapterRegistry.
 * Manages multiple aggregator adapter factories.
 */
class AggregatorAdapterRegistryImpl : AggregatorAdapterRegistry {
    private val factories = mutableMapOf<Aggregator, AggregatorAdapterFactory>()

    override fun getFactory(aggregator: Aggregator): AggregatorAdapterFactory? {
        return when (aggregator) {
            Aggregator.ONEGAMEHUB -> OneGameHubAdapterFactory()
        }
    }

    override fun register(factory: AggregatorAdapterFactory) {
        Aggregator.entries.filter { factory.supports(it) }.forEach { aggregator ->
            factories[aggregator] = factory
        }
    }

    /**
     * Register multiple factories at once.
     */
    fun registerAll(vararg factoryList: AggregatorAdapterFactory) {
        factoryList.forEach { register(it) }
    }
}

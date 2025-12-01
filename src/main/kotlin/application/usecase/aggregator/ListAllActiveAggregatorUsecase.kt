package application.usecase.aggregator

import domain.aggregator.model.AggregatorInfo
import domain.aggregator.repository.AggregatorRepository

/**
 * Use case for listing all active aggregators.
 */
class ListAllActiveAggregatorUsecase(
    private val aggregatorRepository: AggregatorRepository
) {
    suspend operator fun invoke(): List<AggregatorInfo> {
        return aggregatorRepository.findAllActive()
    }
}

package application.usecase.provider

import domain.aggregator.repository.AggregatorRepository
import domain.common.error.NotFoundError
import domain.provider.repository.ProviderRepository
import java.util.UUID

/**
 * Use case for assigning a provider to an aggregator.
 */
class AssignProviderToAggregatorUsecase(
    private val providerRepository: ProviderRepository,
    private val aggregatorRepository: AggregatorRepository
) {
    suspend operator fun invoke(providerId: UUID, aggregatorId: UUID): Result<Unit> {
        providerRepository.findById(providerId)
            ?: return Result.failure(NotFoundError("Provider", providerId.toString()))

        aggregatorRepository.findById(aggregatorId)
            ?: return Result.failure(NotFoundError("Aggregator", aggregatorId.toString()))

        providerRepository.assignToAggregator(providerId, aggregatorId)
        return Result.success(Unit)
    }
}

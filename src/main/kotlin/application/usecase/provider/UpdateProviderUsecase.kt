package application.usecase.provider

import domain.common.error.NotFoundError
import domain.provider.model.Provider
import domain.provider.repository.ProviderRepository

/**
 * Use case for updating a provider.
 */
class UpdateProviderUsecase(
    private val providerRepository: ProviderRepository
) {
    suspend operator fun invoke(
        identity: String,
        order: Int? = null,
        active: Boolean? = null
    ): Result<Provider> {
        val existing = providerRepository.findByIdentity(identity)
            ?: return Result.failure(NotFoundError("Provider", identity))

        val updated = existing.copy(
            order = order ?: existing.order,
            active = active ?: existing.active
        )

        return try {
            val saved = providerRepository.update(updated)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

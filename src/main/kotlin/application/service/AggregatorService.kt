package com.nekgamebling.application.service

import application.port.outbound.CachePort
import application.service.GameService
import domain.aggregator.model.AggregatorInfo
import domain.aggregator.repository.AggregatorRepository
import domain.common.error.NotFoundError
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class AggregatorService(
    private val aggregatorRepository: AggregatorRepository,
    private val cachePort: CachePort
) {

    suspend fun findById(id: UUID): Result<AggregatorInfo> {
        // Check cache first
        cachePort.get<AggregatorInfo>("${CACHE_PREFIX}id:$id")?.let {
            return Result.success(it)
        }

        val aggregatorInfo = aggregatorRepository.findById(id)
            ?: return Result.failure(NotFoundError("Aggregator", id.toString()))

        cachePort.save("${CACHE_PREFIX}id:$id", aggregatorInfo, CACHE_TTL)

        return Result.success(aggregatorInfo)
    }

    companion object {
        private val CACHE_TTL = 5.minutes
        private const val CACHE_PREFIX = "aggregator:"
    }
}
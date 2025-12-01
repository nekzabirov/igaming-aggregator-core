package infrastructure.aggregator.onegamehub.handler

import application.port.outbound.WalletPort
import application.service.SessionService
import domain.common.error.AggregatorError
import domain.common.error.AggregatorNotSupportedError
import domain.common.error.BetLimitExceededError
import domain.common.error.DomainError
import domain.common.error.DuplicateEntityError
import domain.common.error.ExternalServiceError
import domain.common.error.GameUnavailableError
import domain.common.error.IllegalStateError
import domain.common.error.InsufficientBalanceError
import domain.common.error.InvalidPresetError
import domain.common.error.NotFoundError
import domain.common.error.RoundFinishedError
import domain.common.error.RoundNotFoundError
import domain.common.error.SessionInvalidError
import domain.common.error.ValidationError
import infrastructure.aggregator.onegamehub.handler.dto.OneGameHubResponse
import shared.value.SessionToken

class OneGameHubHandler(
    private val sessionService: SessionService,
    private val walletAdapter: WalletPort
) {
    suspend fun balance(token: SessionToken): OneGameHubResponse {
        val session = sessionService.findByToken(token = token).getOrElse {
            return it.toErrorResponse
        }

        val balance = walletAdapter.findBalance(session.playerId).getOrElse {
            return it.toErrorResponse
        }

        return OneGameHubResponse.Success(
            balance = balance.totalAmount,
            currency = balance.currency.value
        )
    }

    private val Throwable.toErrorResponse: OneGameHubResponse.Error
        get() = when (this) {
            is DomainError -> toErrorResponse
            else -> OneGameHubResponse.Error.OneGameHubInvalidateRequest
        }

    private val DomainError.toErrorResponse: OneGameHubResponse.Error
        get() = when (this) {
            is AggregatorError -> TODO()
            is AggregatorNotSupportedError -> TODO()
            is BetLimitExceededError -> TODO()
            is DuplicateEntityError -> TODO()
            is ExternalServiceError -> TODO()
            is GameUnavailableError -> TODO()
            is IllegalStateError -> TODO()
            is InsufficientBalanceError -> TODO()
            is InvalidPresetError -> TODO()
            is NotFoundError -> TODO()
            is RoundFinishedError -> TODO()
            is RoundNotFoundError -> TODO()
            is SessionInvalidError -> OneGameHubResponse.Error.OneGameHubTokenExpired
            is ValidationError -> TODO()
        }
}
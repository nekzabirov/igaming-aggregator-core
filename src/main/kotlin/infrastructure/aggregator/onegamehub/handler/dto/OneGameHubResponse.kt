package infrastructure.aggregator.onegamehub.handler.dto

import kotlinx.serialization.Serializable

@Serializable
sealed class OneGameHubResponse(val status: Int) {
    @Serializable
    data class Success(
        val balance: Int,
        val currency: String,
    ) : OneGameHubResponse(200)

    @Serializable
    data class Error(
        val code: String,
        val display: Boolean,
        val action: String,
        val message: String,
        val description: String,
    ) : OneGameHubResponse(400) {
        companion object {
            val OneGameHubInvalidateRequest = Error(
                code = "ERR001",
                display = false,
                action = "restart",
                message = "Something wrong with request.",
                description = "Invalid request. Please try again."
            )

            val OneGameHubTokenExpired = Error(
                code = "ERR005",
                display = true,
                action = "restart",
                message = "Session expired, please login again.",
                description = "Session expired or not exits"
            )
        }
    }
}

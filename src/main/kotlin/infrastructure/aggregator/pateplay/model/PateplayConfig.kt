package infrastructure.aggregator.pateplay.model

internal class PateplayConfig(private val config: Map<String, String>) {
    val gatewayUrl = config["gatewayUrl"] ?: ""

    val siteCode = config["siteCode"] ?: ""

    val gatewayApiKey = config["gatewayApiKey"] ?: ""

    val gatewayApiSecret = config["gatewayApiSecret"] ?: ""

    val gameLaunchUrl = config["gameLaunchUrl"] ?: ""

    val gameDemoLaunchUrl = config["gameDemoLaunchUrl"] ?: ""

    val walletApiKey = config["walletApiKey"] ?: ""

    val walletApiSecret = config["walletApiSecret"] ?: ""
}

import com.nekgamebling.config.DatabaseConfig
import com.nekgamebling.config.coreModule
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

private val logger by lazy {
    LoggerFactory.getLogger("EventWorker")
}

fun main() {
    // Set timezone before any database operations
    System.setProperty("user.timezone", "UTC")
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))

    embeddedServer(CIO, port = 0, module = Application::module).start(wait = true)
}

fun Application.module() {
    // Initialize database
    DatabaseConfig.init(
        url = System.getenv("DATABASE_URL"),
        driver = "org.postgresql.Driver",
        user = System.getenv("DATABASE_USER"),
        password = System.getenv("DATABASE_PASSWORD")
    )

    install(Koin) {
        slf4jLogger()
        modules(coreModule())
    }

    consumeSpinSettle(System.getenv("RABBITMQ_EXCHANGE"))
}
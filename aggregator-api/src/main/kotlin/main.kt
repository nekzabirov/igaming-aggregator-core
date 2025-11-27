import com.nekgamebling.config.DatabaseConfig
import com.nekgamebling.config.coreModule
import com.nekgamebling.infrastructure.http.aggregatorRoute
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    System.setProperty("user.timezone", "UTC")
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))

    embeddedServer(
        CIO,
        port = 80,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
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

    install(ContentNegotiation) {
        json()
    }

    routing {
        route("webhook") {
            aggregatorRoute()
        }
    }
}

import com.nekgamebling.config.DatabaseConfig
import com.nekgamebling.config.coreModule
import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import service.CollectionServiceImpl
import service.FreespinServiceImpl
import service.GameServiceImpl
import service.ProviderServiceImpl
import service.SessionServiceImpl
import service.SyncServiceImpl

fun main() {
    // Set timezone before any database operations
    System.setProperty("user.timezone", "UTC")
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))

    // Initialize database
    DatabaseConfig.init(
        url = System.getenv("DATABASE_URL"),
        driver = "org.postgresql.Driver",
        user = System.getenv("DATABASE_USER"),
        password = System.getenv("DATABASE_PASSWORD")
    )

    val application = embeddedServer(CIO, port = 0) {
        install(Koin) {
            slf4jLogger()
            modules(coreModule())
        }
    }
    application.start(wait = false)

    val server: Server = NettyServerBuilder
        .forPort(5050)
        .addService(SyncServiceImpl(application.application))
        .addService(CollectionServiceImpl(application.application))
        .addService(ProviderServiceImpl(application.application))
        .addService(GameServiceImpl(application.application))
        .addService(SessionServiceImpl(application.application))
        .addService(FreespinServiceImpl(application.application))
        .build()
        .start()

    server.awaitTermination()
}
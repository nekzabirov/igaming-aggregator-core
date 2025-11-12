import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import service.SyncServiceImpl

fun main() {
    // Set timezone before any database operations
    System.setProperty("user.timezone", "UTC")
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))

    val application = embeddedServer(CIO, port = 0) {
        install(Koin) {
            slf4jLogger()
        }

        install(SharedPlugin) {
            databaseUrl = "jdbc:postgresql://localhost:5432/catalog?TimeZone=UTC"
            databaseDriver = "org.postgresql.Driver"
            databaseUser = "app"
            databasePassword = "app_password"
            autoCreateSchema = true
            showSql = false
        }
    }
    application.start(wait = false)

    val syncService = SyncServiceImpl(application.application)

    val server: Server = NettyServerBuilder
        .forPort(8080)
        .addService(syncService)
        .build()
        .start()

    server.awaitTermination()
}
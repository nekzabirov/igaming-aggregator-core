import app.usecase.AddAggregatorUsecase
import app.usecase.ListAllActiveAggregatorUsecase
import app.usecase.ListCollectionUsecase
import app.usecase.SyncGameUsecase
import io.ktor.server.application.Application
import org.koin.dsl.module

val Application.sharedModule
    get() = module {
        //Usecase
        factory { AddAggregatorUsecase() }
        factory { ListAllActiveAggregatorUsecase() }
        factory { ListCollectionUsecase() }
        factory { SyncGameUsecase() }
    }
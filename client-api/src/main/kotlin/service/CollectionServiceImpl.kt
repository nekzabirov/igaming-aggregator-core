package service

import com.nekzabirov.igambling.proto.dto.EmptyResult
import com.nekzabirov.igambling.proto.service.AddCollectionCommand
import com.nekzabirov.igambling.proto.service.CollectionGrpcKt
import core.value.LocaleName
import io.grpc.Status
import io.grpc.StatusException
import io.ktor.server.application.Application
import org.koin.ktor.ext.get
import usecase.AddCollectionUsecase


class CollectionServiceImpl(application: Application) : CollectionGrpcKt.CollectionCoroutineImplBase() {
    private val addCollectionUsecase = application.get<AddCollectionUsecase>()

    override suspend fun addCollection(request: AddCollectionCommand): EmptyResult {
        addCollectionUsecase(request.identity, LocaleName(request.nameMap))
            .getOrElse { throw StatusException(Status.INVALID_ARGUMENT.withDescription(it.message)) }

        return EmptyResult.getDefaultInstance()
    }
}
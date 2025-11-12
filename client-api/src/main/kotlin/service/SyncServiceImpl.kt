package service

import app.usecase.AddAggregatorUsecase
import app.usecase.ListAggregatorUsecase
import com.nekzabirov.igambling.proto.service.AddAggregatorCommand
import com.nekzabirov.igambling.proto.service.EmptyResult
import com.nekzabirov.igambling.proto.service.ListAggregatorCommand
import com.nekzabirov.igambling.proto.service.ListAggregatorResult
import com.nekzabirov.igambling.proto.service.SyncGrpcKt
import domain.model.AggregatorInfo
import domain.value.Aggregator
import domain.value.Pageable
import io.grpc.Status
import io.grpc.StatusException
import io.ktor.server.application.*
import mapper.toAggregatorProto
import mapper.toProto
import org.koin.ktor.ext.get

class SyncServiceImpl(application: Application) : SyncGrpcKt.SyncCoroutineImplBase() {
    private val addAggregatorUsecase = application.get<AddAggregatorUsecase>()
    private val listAggregatorUsecase = application.get<ListAggregatorUsecase>()

    override suspend fun addAggregator(request: AddAggregatorCommand): EmptyResult {
        val type = Aggregator.valueOf(request.type)

        return addAggregatorUsecase(request.identity, type, request.configMap)
            .map { EmptyResult.getDefaultInstance() }
            .getOrElse { throw StatusException(Status.INVALID_ARGUMENT.withDescription(it.message)) }
    }

    override suspend fun listAggregator(request: ListAggregatorCommand): ListAggregatorResult {
        val pageable = Pageable(page = request.pageNumber, size = request.pageSize)

        return listAggregatorUsecase(pageable) {
            it.withQuery(request.query)

            if (request.hasActive()) {
                it.withActive(request.active)
            }

            if (request.hasType()) {
                it.withActive(request.active)
            }
        }
            .let {
                ListAggregatorResult.newBuilder()
                    .setTotalPage(it.totalPages.toInt())
                    .addAllItems(it.items.map { a: AggregatorInfo -> a.toAggregatorProto() })
                    .build()
            }
    }
}
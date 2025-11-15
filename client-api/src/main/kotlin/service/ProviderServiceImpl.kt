package service

import com.nekzabirov.igambling.proto.service.ListProviderCommand
import com.nekzabirov.igambling.proto.service.ListProviderResult
import com.nekzabirov.igambling.proto.service.ProviderGrpcKt
import core.value.Pageable
import io.ktor.server.application.*
import mapper.toAggregatorProto
import mapper.toProviderProto
import org.koin.ktor.ext.get
import usecase.ProviderListUsecase

class ProviderServiceImpl(application: Application) : ProviderGrpcKt.ProviderCoroutineImplBase() {
    private val providerListUsecase = application.get<ProviderListUsecase>()

    override suspend fun list(request: ListProviderCommand): ListProviderResult =
        providerListUsecase(pageable = Pageable(request.pageNumber, request.pageSize)) {
            withQuery(request.query)

            if (request.hasActive()) {
                withActive(request.active)
            }
        }
            .let {
                val items = it.items.map { i ->
                    ListProviderResult.Item
                        .newBuilder()
                        .setActiveGames(i.activeGamesCount)
                        .setTotalGames(i.totalGamesCount)
                        .setProvider(i.provider.toProviderProto())
                        .build()
                }

                val aggregators = it.items
                    .map { i -> i.aggregatorInfo }
                    .toSet()
                    .map { i -> i.toAggregatorProto() }

                ListProviderResult.newBuilder()
                    .setTotalPage(it.totalPages.toInt())
                    .addAllItems(items)
                    .addAllAggregators(aggregators)
                    .build()
            }
}
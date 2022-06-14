package ru.wb.go.ui.courierbilling.domain

import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.utils.managers.DeviceManager

class CourierBillingInteractorImpl(
    rxSchedulerFactory: RxSchedulerFactory,
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val appRemoteRepository: AppRemoteRepository,
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierBillingInteractor {

    override suspend fun getBillingInfo(): BillingCommonEntity {
        return withContext(Dispatchers.IO){
            appRemoteRepository.getBillingInfo(true)
        }
    }

//    override fun getBillingInfo(): Single<BillingCommonEntity> {
//        return appRemoteRepository.getBillingInfo(true)
//            .compose(rxSchedulerFactory.applySingleSchedulers())
//    }


}
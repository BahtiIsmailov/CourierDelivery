package ru.wb.go.ui.courierbilling.domain

import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.BillingCommonEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.utils.managers.DeviceManager

class CourierBillingInteractorImpl(
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val appRemoteRepository: AppRemoteRepository,
) : BaseServiceInteractorImpl(networkMonitorRepository, deviceManager),
    CourierBillingInteractor {

    override suspend fun getBillingInfo(): BillingCommonEntity {
        return appRemoteRepository.getBillingInfo(true)

    }


}
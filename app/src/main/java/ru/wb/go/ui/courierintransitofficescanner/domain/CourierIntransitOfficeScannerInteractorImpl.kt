package ru.wb.go.ui.courierintransitofficescanner.domain

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.scanner.domain.ScannerAction
import ru.wb.go.ui.scanner.domain.ScannerRepository
import ru.wb.go.ui.scanner.domain.ScannerState
import ru.wb.go.utils.managers.DeviceManager

class CourierIntransitOfficeScannerInteractorImpl(
    rxSchedulerFactory: RxSchedulerFactory,
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val locRepo: CourierLocalRepository,
    private val scannerRepo: ScannerRepository,
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierIntransitOfficeScannerInteractor {


     override fun getOffices(): Flow<List<LocalOfficeEntity>> {
         return locRepo.getOfficesFlowable()
             .map { office ->
               office.toMutableList().sortedWith(
                    compareBy({ it.isVisited }, { it.deliveredBoxes == it.countBoxes })
                )
             }
     }

// override fun getOffices(): Observable<List<LocalOfficeEntity>> {
//        return locRepo.getOfficesFlowable()
//            .toObservable()
//            .map { office ->
//                office.toMutableList().sortedWith(
//                    compareBy({ it.isVisited }, { it.deliveredBoxes == it.countBoxes })
//                )
//            }
//            .compose(rxSchedulerFactory.applyObservableSchedulers())
//    }


    @OptIn(FlowPreview::class)
    override fun observeOfficeIdScanProcess(): Flow<CourierIntransitOfficeScanData> {
        return scannerRepo.observeScannerAction()
            .flatMapMerge{
                flowOf(
                    when (it) {
                        is ScannerAction.HoldSplashUnlock -> CourierIntransitOfficeScanData.HoldSplashUnlock
                        is ScannerAction.HoldSplashLock -> CourierIntransitOfficeScanData.HoldSplashLock
                        is ScannerAction.ScanResult -> scanResult(it)
                    }
                )
            }
    }
//
//    override fun observeOfficeIdScanProcess(): Observable<CourierIntransitOfficeScanData> {
//        return scannerRepo.observeScannerAction()
//            .flatMap { convertScannerAction(it) }
//            .compose(rxSchedulerFactory.applyObservableSchedulers())
//    }

    private suspend fun scanResult(it: ScannerAction.ScanResult): CourierIntransitOfficeScanData {
        val parse = scannerRepo.parseScanOfficeQr(it.value)
        return when (parse.isOk) {
            true -> {
                if (locRepo.getOffices().find { it.officeId == parse.officeId } == null) {
                    CourierIntransitOfficeScanData.WrongOfficeScan
                } else {
                    locRepo.visitOffice(parse.officeId)
                    CourierIntransitOfficeScanData.NecessaryOfficeScan(parse.officeId)
                }
            }
            else -> CourierIntransitOfficeScanData.UnknownQrOfficeScan
        }
    }
//
//    private fun scanResult(it: ScannerAction.ScanResult): CourierIntransitOfficeScanData {
//        val parse = scannerRepo.parseScanOfficeQr(it.value)
//        return when (parse.isOk) {
//            true -> {
//                if (locRepo.getOffices().find { it.officeId == parse.officeId } == null) {
//                    CourierIntransitOfficeScanData.WrongOfficeScan
//                } else {
//                    locRepo.visitOffice(parse.officeId)
//                    CourierIntransitOfficeScanData.NecessaryOfficeScan(parse.officeId)
//                }
//            }
//            else -> CourierIntransitOfficeScanData.UnknownQrOfficeScan
//        }
//    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepo.scannerState(scannerAction)
    }
}

/*

    override fun observeOfficeIdScanProcess(): Observable<CourierIntransitOfficeScanData> {
        return scannerRepo.observeScannerAction()
            .flatMap { convertScannerAction(it) }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun convertScannerAction(it: ScannerAction) = Single.just(
        when (it) {
            ScannerAction.HoldSplashUnlock -> CourierIntransitOfficeScanData.HoldSplashUnlock
            ScannerAction.HoldSplashLock -> CourierIntransitOfficeScanData.HoldSplashLock
            is ScannerAction.ScanResult -> scanResult(it)
        }
    ).toObservable()

    private fun scanResult(it: ScannerAction.ScanResult): CourierIntransitOfficeScanData {
        val parse = scannerRepo.parseScanOfficeQr(it.value)
        return when (parse.isOk) {
            true -> {
                if (locRepo.getOffices().find { it.officeId == parse.officeId } == null) {
                    CourierIntransitOfficeScanData.WrongOfficeScan
                } else {
                    locRepo.visitOffice(parse.officeId)
                    CourierIntransitOfficeScanData.NecessaryOfficeScan(parse.officeId)
                }
            }
            else -> CourierIntransitOfficeScanData.UnknownQrOfficeScan
        }
    }

    override fun scannerAction(scannerAction: ScannerState) {
        scannerRepo.scannerState(scannerAction)
    }


 */

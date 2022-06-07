package ru.wb.go.ui.scanner.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.wb.go.app.PREFIX_BOX_QR_CODE_SPLITTER_V1
import ru.wb.go.app.PREFIX_BOX_QR_CODE_V1
import ru.wb.go.app.PREFIX_QR_OFFICE_CODE_OLD
import ru.wb.go.app.PREFIX_QR_OFFICE_CODE_V1
import ru.wb.go.network.api.app.entity.ParsedScanBoxQrEntity
import ru.wb.go.network.api.app.entity.ParsedScanOfficeQrEntity
import ru.wb.go.ui.courierloading.domain.CourierLoadingInteractorImpl.Companion.DELAY_HOLD_SCANNER
import ru.wb.go.utils.time.TimeFormatter
import java.util.concurrent.TimeUnit

class ScannerRepositoryImpl(private val timeFormatter: TimeFormatter) : ScannerRepository {

//    private var scannerActionSubject = PublishSubject.create<ScannerAction>() //
    private var scannerActionSubject = PublishSubject.create<ScannerAction>()
    private val scannerStateSubject = PublishSubject.create<ScannerState>()

    override fun scannerAction(action: ScannerAction) {
        scannerActionSubject.onNext(action)
    }

    override fun observeScannerAction(): ScannerAction  {
        return ScannerAction.
    }

    override fun scannerState(state: ScannerState) {
        scannerStateSubject.onNext(state)
    }

    override fun observeScannerState(): Observable<ScannerState> {
        return scannerStateSubject
    }

    override fun parseScanBoxQr(qrCode: String): ParsedScanBoxQrEntity {
        val result = ParsedScanBoxQrEntity("", "", false)
        if (!qrCode.startsWith(PREFIX_BOX_QR_CODE_V1)) return result
        val parseParams = qrCode.split(PREFIX_BOX_QR_CODE_SPLITTER_V1)
        if (parseParams.size != 4) return result
        parseParams[3].toIntOrNull() ?: return result
        parseParams[2].toIntOrNull() ?: return result
        return ParsedScanBoxQrEntity(parseParams[2], parseParams[3], isOk = true)
    }

    override fun holdStart(): Completable =
        Observable.timer(DELAY_HOLD_SCANNER, TimeUnit.MILLISECONDS)
            .doOnNext { scannerState(ScannerState.StartScan) }
            .flatMapCompletable { Completable.complete() }

    override fun parseScanOfficeQr(qrCode: String): ParsedScanOfficeQrEntity {

        return when {
            qrCode.startsWith(PREFIX_QR_OFFICE_CODE_OLD) -> {
                val code = qrCode.split(".")
                if (code.size != 3) {
                    ParsedScanOfficeQrEntity(-1, false)
                } else {
                    val ofId = code[1].toIntOrNull()
                    if (ofId == null) {
                        ParsedScanOfficeQrEntity(-1, false)
                    } else
                        ParsedScanOfficeQrEntity(ofId, true)
                }
            }
            qrCode.startsWith(PREFIX_QR_OFFICE_CODE_V1) -> {
                getSplitDynamicOfficeInfo(qrCode)
            }
            else -> ParsedScanOfficeQrEntity(-1, false)
        }

    }

    private fun getSplitDynamicOfficeInfo(input: String): ParsedScanOfficeQrEntity {
        val splitter = input.split(";")
        val officeId = splitter[0].replace("o:", "")

        val ofId = officeId.toIntOrNull() ?: return ParsedScanOfficeQrEntity(-1, false)

        val officeHash = splitter[1].replace("c:", "")
        val currentOfficeHash = getHashByOfficeId(ofId)
        return if (currentOfficeHash == officeHash) ParsedScanOfficeQrEntity(ofId, true)
        else ParsedScanOfficeQrEntity(-1, false)
    }

    private fun getHashByOfficeId(officeId: Int): String {
        val startTime =
            timeFormatter.dateTimeFromStringSimple("2020-01-01T00:00:00.000+03:00").millis
        val currentTime = timeFormatter.currentDateTime().millis
        val dif = (currentTime - startTime) / 86400 / 1000
        val m = (31 * dif * officeId).toString()
        return m.takeLast(4)
    }

}
package ru.wb.go.ui.scanner.domain

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.wb.go.app.PREFIX_BOX_QR_CODE_SPLITTER_V1
import ru.wb.go.app.PREFIX_BOX_QR_CODE_V1
import ru.wb.go.app.PREFIX_QR_OFFICE_CODE_V1
import ru.wb.go.network.api.app.entity.ParsedScanBoxQrEntity
import ru.wb.go.network.api.app.entity.ParsedScanOfficeQrEntity
import ru.wb.go.ui.courierloading.domain.CourierLoadingInteractorImpl.Companion.DELAY_HOLD_SCANNER
import ru.wb.go.utils.time.TimeFormatter

class ScannerRepositoryImpl(private val timeFormatter: TimeFormatter
) : ScannerRepository {


    private var scannerActionSubject = MutableSharedFlow<ScannerAction>(
        replay = 1,
        extraBufferCapacity = Int.MAX_VALUE,
        onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private var scannerStateSubject = MutableSharedFlow<ScannerState>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun clearScannerState() {
        scannerActionSubject = MutableSharedFlow(
            replay = 1,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.DROP_OLDEST)

        scannerStateSubject = MutableSharedFlow(
            replay = 1,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }
     override fun scannerAction(action:ScannerAction){
         Log.e("scannerAction","emit:$action")
         scannerActionSubject.tryEmit(action)
    }

    override fun observeScannerAction(): Flow<ScannerAction> {
       return scannerActionSubject
    }

    override fun scannerState(state: ScannerState) {
        scannerStateSubject.tryEmit(state)
        //scannerStateSubject.trySend(state)
    }

    override fun observeScannerState(): Flow<ScannerState> {
        return scannerStateSubject//.receiveAsFlow()
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

    override suspend fun holdStart(){
        delay(DELAY_HOLD_SCANNER)
        scannerState(ScannerState.StartScan)
    }


    override fun parseScanOfficeQr(qrCode: String): ParsedScanOfficeQrEntity {
        return when {
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


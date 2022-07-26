package ru.wb.go.ui.scanner.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.api.app.entity.ParsedScanBoxQrEntity
import ru.wb.go.network.api.app.entity.ParsedScanOfficeQrEntity

interface ScannerRepository {

    fun scannerAction(action: ScannerAction)

    fun observeScannerAction(): Flow<ScannerAction>

    fun scannerState(state: ScannerState)

    fun observeScannerState(): Flow<ScannerState>

    fun parseScanBoxQr(qrCode: String): ParsedScanBoxQrEntity

    fun parseScanOfficeQr(qrCode: String): ParsedScanOfficeQrEntity

    fun clearScannerState()

    suspend fun holdStart()
}

package ru.wb.go.ui.scanner.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.api.app.entity.ParsedScanBoxQrEntity
import ru.wb.go.network.api.app.entity.ParsedScanOfficeQrEntity

interface ScannerRepository {

    suspend fun scannerAction(action: ScannerAction)

    suspend fun observeScannerAction(): Flow<ScannerAction>

    suspend fun scannerState(state: ScannerState)

    suspend fun observeScannerState(): Flow<ScannerState>

    fun parseScanBoxQr(qrCode: String): ParsedScanBoxQrEntity

    fun parseScanOfficeQr(qrCode: String): ParsedScanOfficeQrEntity

    suspend fun holdStart()
}

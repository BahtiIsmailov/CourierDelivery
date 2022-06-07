package ru.wb.go.ui.scanner.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.go.network.api.app.entity.ParsedScanBoxQrEntity
import ru.wb.go.network.api.app.entity.ParsedScanOfficeQrEntity

interface ScannerRepository {

    fun scannerAction(action: ScannerAction)

    fun observeScannerAction():  Observable<ScannerAction>

    fun scannerState(state: ScannerState)

    fun observeScannerState(): Observable<ScannerState>

    fun parseScanBoxQr(qrCode: String): ParsedScanBoxQrEntity

    fun parseScanOfficeQr(qrCode: String): ParsedScanOfficeQrEntity

    fun holdStart() : Completable
}
package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.functions.Action

interface ScannerInteractor {

    suspend fun observeHoldSplash():  Action

    fun barcodeScanned(barcode: String)

    fun holdSplashUnlock()

    fun prolongHoldTimer()

    suspend fun observeScannerState():  ScannerState

}
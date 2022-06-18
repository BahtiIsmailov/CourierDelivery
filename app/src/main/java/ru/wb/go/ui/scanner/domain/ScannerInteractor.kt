package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.functions.Action
import kotlinx.coroutines.flow.Flow

interface ScannerInteractor {

      fun observeHoldSplash(): Flow<Action>

      fun barcodeScanned(barcode: String)

      fun holdSplashUnlock()

      fun prolongHoldTimer()

      fun observeScannerState(): Flow<ScannerState>

}

/*
   fun observeHoldSplash(): Observable<Action>

    fun barcodeScanned(barcode: String)

    fun holdSplashUnlock()

    fun prolongHoldTimer()

    fun observeScannerState(): Observable<ScannerState>
 */
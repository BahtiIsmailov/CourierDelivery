package ru.wb.go.ui.scanner.domain

import io.reactivex.Observable
import io.reactivex.functions.Action
import kotlinx.coroutines.flow.Flow

interface ScannerInteractor {

    suspend fun observeHoldSplash(): Flow<Action>

    suspend fun barcodeScanned(barcode: String)

    suspend fun holdSplashUnlock()

    suspend fun prolongHoldTimer()

    suspend fun observeScannerState(): Flow<ScannerState>

}
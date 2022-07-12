package ru.wb.go.ui.scanner.domain

import kotlinx.coroutines.flow.Flow

interface ScannerInteractor {

      fun observeHoldSplash(): Flow<Unit>

      fun barcodeScanned(barcode: String)

      fun holdSplashUnlock()

      fun prolongHoldTimer()

      fun observeScannerState(): Flow<ScannerState>


}


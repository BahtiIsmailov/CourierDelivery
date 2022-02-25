package ru.wb.go.ui.scanner.domain

sealed class ScannerState {

    object StopScan : ScannerState()
    object StartScan : ScannerState()

    object StopScanWithHoldSplash : ScannerState()

    object HoldScanComplete : ScannerState()
    object HoldScanError : ScannerState()
    object HoldScanUnknown : ScannerState()

}
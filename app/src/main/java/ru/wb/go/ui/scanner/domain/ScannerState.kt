package ru.wb.go.ui.scanner.domain

sealed class ScannerState {

    object StopScan : ScannerState()
    object Start : ScannerState()

    object HoldScanComplete : ScannerState()
    object HoldScanError : ScannerState()
    object HoldScanUnknown : ScannerState()

}
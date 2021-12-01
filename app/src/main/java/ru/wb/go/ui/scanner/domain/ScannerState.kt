package ru.wb.go.ui.scanner.domain

sealed class ScannerState {

    object Stop : ScannerState()

    object Start : ScannerState()

    object LoaderProgress : ScannerState()

    object LoaderComplete : ScannerState()

    object BeepScan : ScannerState()

    object HoldScanComplete : ScannerState()
    object HoldScanError : ScannerState()
    object HoldScanUnknown : ScannerState()

}
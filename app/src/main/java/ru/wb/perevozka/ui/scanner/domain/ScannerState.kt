package ru.wb.perevozka.ui.scanner.domain

sealed class ScannerState {

    object Stop : ScannerState()

    object Start : ScannerState()

    object LoaderProgress : ScannerState()

    object LoaderComplete : ScannerState()

    object BeepScan : ScannerState()

}
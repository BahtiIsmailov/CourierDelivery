package ru.wb.perevozka.ui.scanner.domain

sealed class ScannerAction {

    object Stop : ScannerAction()

    object Start : ScannerAction()

    object LoaderProgress : ScannerAction()

    object LoaderComplete : ScannerAction()

    object BeepScan : ScannerAction()

}
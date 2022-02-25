package ru.wb.go.ui.scanner.domain

sealed class ScannerAction {

    data class ScanResult(val value: String) : ScannerAction()

    object HoldSplashUnlock : ScannerAction()

    object HoldSplashLock : ScannerAction()

}
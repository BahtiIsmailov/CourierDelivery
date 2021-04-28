package com.wb.logistics.ui.scanner.domain

sealed class ScannerAction {

    object Stop : ScannerAction()

    object Start : ScannerAction()

}
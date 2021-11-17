package ru.wb.go.ui.dcunloading.domain

sealed class DcUnloadingAction {

    data class BoxUnloaded(val barcode: String) : DcUnloadingAction()

    object BoxDoesNotBelongFlight : DcUnloadingAction()

    object BoxDoesNotBelongDc : DcUnloadingAction()

    data class BoxDoesNotBelongInfoEmpty(val barcode: String) : DcUnloadingAction()

    data class BoxAlreadyUnloaded(val barcode: String) : DcUnloadingAction()

    object Init : DcUnloadingAction()

}
package ru.wb.go.ui.courierintransitofficescanner

sealed class CourierIntransitOfficeScannerNavigationState {

    object NavigateToMap : CourierIntransitOfficeScannerNavigationState()

    data class NavigateToUnloadingScanner(val officeId: Int) :
        CourierIntransitOfficeScannerNavigationState()

    data class NavigateToOfficeFailed(val title: String, val message: String) :
        CourierIntransitOfficeScannerNavigationState()

    object NavigateToScanner : CourierIntransitOfficeScannerNavigationState()

}

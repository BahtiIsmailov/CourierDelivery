package ru.wb.go.ui.courieragreement

sealed class CourierAgreementNavigationState {
    object Cancel : CourierAgreementNavigationState()
    object Complete : CourierAgreementNavigationState()
}
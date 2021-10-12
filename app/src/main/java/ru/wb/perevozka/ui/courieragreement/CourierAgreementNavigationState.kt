package ru.wb.perevozka.ui.courieragreement

sealed class CourierAgreementNavigationState {
    object Cancel : CourierAgreementNavigationState()
    object Complete : CourierAgreementNavigationState()
}
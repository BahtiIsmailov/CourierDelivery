package ru.wb.go.ui.courierdatatype

import ru.wb.go.network.api.app.entity.CourierDocumentsEntity

sealed class CourierDataTypeNavAction {

    data class NavigateToCourierData(
        val phone: String,
        val docs: CourierDocumentsEntity
    ) : CourierDataTypeNavAction()

}
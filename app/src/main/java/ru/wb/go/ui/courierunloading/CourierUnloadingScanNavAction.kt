package ru.wb.go.ui.courierunloading

import ru.wb.go.db.entity.courierlocal.LocalBoxEntity

sealed class CourierUnloadingScanNavAction {

    object NavigateToIntransit : CourierUnloadingScanNavAction()

    data class InitAndShowUnloadingItems(val items: MutableList<RemainBoxItem>,val localBoxEntity: List<LocalBoxEntity>) :
        CourierUnloadingScanNavAction()

    object HideUnloadingItems : CourierUnloadingScanNavAction()

}
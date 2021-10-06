package ru.wb.perevozka.ui.couriermap

sealed class CourierMapAction {

    data class ItemClick(val index: String) : CourierMapAction()

    object PermissionComplete : CourierMapAction()

}
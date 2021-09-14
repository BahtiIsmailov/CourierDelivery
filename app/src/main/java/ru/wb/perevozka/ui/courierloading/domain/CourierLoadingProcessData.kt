package ru.wb.perevozka.ui.courierloading.domain

import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity

data class CourierLoadingProcessData(val scanBoxData: CourierLoadingScanBoxData, val boxes : List<CourierBoxEntity>, val count: Int)

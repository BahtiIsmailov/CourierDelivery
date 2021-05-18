package com.wb.logistics.ui.flightdeliveriesdetails.domain

import com.wb.logistics.db.entity.returnboxes.ReturnBoxByAddressEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity

data class UnloadedAndReturnBoxesGroupByOffice(
    val unloadedBoxes: List<UnloadedBoxEntity>,
    val returnBoxes: List<ReturnBoxByAddressEntity>,
)

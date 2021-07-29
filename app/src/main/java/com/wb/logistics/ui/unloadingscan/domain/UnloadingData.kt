package com.wb.logistics.ui.unloadingscan.domain

import com.wb.logistics.db.entity.unload.UnloadingTookAndPickupCountEntity
import com.wb.logistics.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity

data class UnloadingData(
    val unloadingAction: UnloadingAction,
    val flightUnloadedAndUnloadCountEntity: UnloadingUnloadedAndUnloadCountEntity,
    val flightTookAndPickupCountEntity: UnloadingTookAndPickupCountEntity,
)



package ru.wb.go.ui.unloadingscan.domain

import ru.wb.go.db.entity.unload.UnloadingTookAndPickupCountEntity
import ru.wb.go.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity

data class UnloadingData(
    val unloadingAction: UnloadingAction,
    val flightUnloadedAndUnloadCountEntity: UnloadingUnloadedAndUnloadCountEntity,
    val flightTookAndPickupCountEntity: UnloadingTookAndPickupCountEntity,
)



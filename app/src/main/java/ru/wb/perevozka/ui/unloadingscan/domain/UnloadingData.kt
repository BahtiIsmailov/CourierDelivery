package ru.wb.perevozka.ui.unloadingscan.domain

import ru.wb.perevozka.db.entity.unload.UnloadingTookAndPickupCountEntity
import ru.wb.perevozka.db.entity.unload.UnloadingUnloadedAndUnloadCountEntity

data class UnloadingData(
    val unloadingAction: UnloadingAction,
    val flightUnloadedAndUnloadCountEntity: UnloadingUnloadedAndUnloadCountEntity,
    val flightTookAndPickupCountEntity: UnloadingTookAndPickupCountEntity,
)



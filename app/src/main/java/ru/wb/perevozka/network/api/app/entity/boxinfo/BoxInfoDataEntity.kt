package ru.wb.perevozka.network.api.app.entity.boxinfo

import ru.wb.perevozka.db.Optional

data class BoxInfoDataEntity(
    val box: Optional<BoxInfoEntity>,
    val flight: Optional<BoxInfoFlightEntity>,
)
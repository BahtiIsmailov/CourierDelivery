package ru.wb.go.network.api.app.entity.boxinfo

import ru.wb.go.db.Optional

data class BoxInfoDataEntity(
    val box: Optional<BoxInfoEntity>,
    val flight: Optional<BoxInfoFlightEntity>,
)
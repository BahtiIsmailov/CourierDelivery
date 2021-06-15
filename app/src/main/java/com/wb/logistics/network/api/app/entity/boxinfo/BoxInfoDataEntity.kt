package com.wb.logistics.network.api.app.entity.boxinfo

import com.wb.logistics.db.Optional

data class BoxInfoDataEntity(
    val box: Optional<BoxInfoEntity>,
    val flight: Optional<BoxInfoFlightEntity>,
)
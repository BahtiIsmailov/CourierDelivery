package ru.wb.go.ui.unloadingforcedtermination

import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.utils.time.TimeFormatType.ONLY_DATE
import ru.wb.go.utils.time.TimeFormatType.ONLY_TIME
import ru.wb.go.utils.time.TimeFormatter

class ForcedTerminationDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: ForcedTerminationResourceProvider,
) : ForcedTerminationDataBuilder {

    override fun buildForcedTerminationItem(value: IndexedValue<FlightBoxEntity>): ForcedTerminationItem {
        val item = value.value
        val dateFormat = timeFormatter.dateTimeWithoutTimezoneFromString(item.updatedAt)
        val data = resourceProvider.getNotDeliveryDate(
            timeFormatter.format(dateFormat, ONLY_DATE),
            timeFormatter.format(dateFormat, ONLY_TIME))
        val indexUnnamedBarcode =
            resourceProvider.getIndexUnnamedBarcode(value.index + 1, item.barcode)
        return ForcedTerminationItem(indexUnnamedBarcode, data)
    }

}
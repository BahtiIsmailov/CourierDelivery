package ru.wb.go.ui.dcunloadingforcedtermination

import ru.wb.go.db.entity.dcunloadedboxes.DcNotUnloadedBoxEntity
import ru.wb.go.utils.time.TimeFormatType.ONLY_DATE
import ru.wb.go.utils.time.TimeFormatType.ONLY_TIME
import ru.wb.go.utils.time.TimeFormatter

class DcForcedTerminationDetailsDataBuilderImpl(
    private val timeFormatter: TimeFormatter,
    private val resourceProvider: DcForcedTerminationDetailsResourceProvider,
) : DcForcedTerminationDetailsDataBuilder {

    override fun buildDcForcedTerminationItem(value: IndexedValue<DcNotUnloadedBoxEntity>): DcForcedTerminationDetailsItem {
        val item = value.value
        val date = timeFormatter.dateTimeWithoutTimezoneFromString(item.updatedAt)
        val dcOffice = item.currentOffice
        val srcOffice = item.srcOffice
        val data = if (dcOffice == srcOffice) {
            resourceProvider.getNotDelivery(
                timeFormatter.format(date, ONLY_DATE),
                timeFormatter.format(date, ONLY_TIME))
        } else {
            resourceProvider.getNotReturned(
                timeFormatter.format(date, ONLY_DATE),
                timeFormatter.format(date, ONLY_TIME),
                item.srcFullAddress)
        }

        return DcForcedTerminationDetailsItem(
            resourceProvider.getUnnamedBarcodeFormat(value.index + 1, item.barcode),
            data)
    }

}
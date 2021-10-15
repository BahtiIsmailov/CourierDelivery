package ru.wb.perevozka.ui.courierbilling

import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.network.api.app.entity.BillingTransactionEntity
import ru.wb.perevozka.ui.courierbilling.delegates.items.CourierBillingNegativeItem
import ru.wb.perevozka.ui.courierbilling.delegates.items.CourierBillingPositiveItem
import ru.wb.perevozka.utils.time.TimeFormatType
import ru.wb.perevozka.utils.time.TimeFormatter
import java.text.DecimalFormat

class CourierBillingDataBuilderImpl(
    private val resourceProvider: CourierBillingResourceProvider,
    private val timeFormatter: TimeFormatter,
) : CourierBillingDataBuilder {

    override fun buildOrderItem(index: Int, entity: BillingTransactionEntity): BaseItem {

        val amount = entity.value
        val decimal = DecimalFormat("#,###.##")

        val date = timeFormatter.dateTimeWithoutTimezoneFromString(entity.createdAt)
        val dateFormat = timeFormatter.format(date, TimeFormatType.ONLY_DATE)
        val timeFormat = timeFormatter.format(date, TimeFormatType.ONLY_TIME)
        val formatDate = resourceProvider.getBoxDateAndTime(dateFormat, timeFormat)

        return if (amount > 0) {
            val formatAmount = decimal.format(amount)
            CourierBillingPositiveItem(
                date = formatDate,
                amount = "+ " + formatAmount + " ₽",
                idView = index
            )

        } else {
            val formatAmount = decimal.format(amount * -1)
            CourierBillingNegativeItem(
                date = formatDate,
                amount = "- " + formatAmount + " ₽",
                idView = index
            )
        }
    }

}
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

        val dateTime = timeFormatter.dateTimeWithoutTimezoneFromString(entity.createdAt)
        val dateFormat = timeFormatter.format(dateTime, TimeFormatType.ONLY_DATE)
        val time = timeFormatter.format(dateTime, TimeFormatType.ONLY_TIME)
        val timeFormat = resourceProvider.getBillingTime(time)

        return if (amount > 0) {
            val formatAmount = decimal.format(amount)
            CourierBillingPositiveItem(
                date = dateFormat,
                time = timeFormat,
                amount = resourceProvider.getPositiveAmount(formatAmount),
                idView = index
            )

        } else {
            val formatAmount = decimal.format(amount * -1)
            CourierBillingNegativeItem(
                date = dateFormat,
                time = timeFormat,
                amount = resourceProvider.getNegativeAmount(formatAmount),
                idView = index
            )
        }
    }

}
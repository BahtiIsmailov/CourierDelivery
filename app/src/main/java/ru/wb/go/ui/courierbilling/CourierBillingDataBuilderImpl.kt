package ru.wb.go.ui.courierbilling

import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.entity.BillingTransactionEntity
import ru.wb.go.ui.courierbilling.delegates.items.CourierBillingPositiveItem
import ru.wb.go.utils.time.TimeFormatType
import ru.wb.go.utils.time.TimeFormatter
import java.text.DecimalFormat

class CourierBillingDataBuilderImpl(
    private val resourceProvider: CourierBillingResourceProvider,
    private val timeFormatter: TimeFormatter,
) : CourierBillingDataBuilder {

    override fun buildOrderItem(index: Int, entity: BillingTransactionEntity): BaseItem {

        val amount = entity.value

        val dateTime = timeFormatter.dateTimeWithoutTimezoneFromString(entity.createdAt)
        val dateFormat = timeFormatter.format(dateTime, TimeFormatType.ONLY_DATE)
        val time = timeFormatter.format(dateTime, TimeFormatType.ONLY_TIME)
        val timeFormat = resourceProvider.getBillingTime(time)

        return CourierBillingPositiveItem(
            date = dateFormat,
            time = timeFormat,
            amount = resourceProvider.formatMoney(amount,  true),
            idView = index
        )

    }

}
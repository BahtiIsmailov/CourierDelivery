package ru.wb.go.ui.courierbilling

import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.StatusOK
import ru.wb.go.network.api.app.entity.BillingTransactionEntity
import ru.wb.go.ui.courierbilling.delegates.items.CourierBillingNegativeItem
import ru.wb.go.ui.courierbilling.delegates.items.CourierBillingPositiveItem
import ru.wb.go.utils.time.TimeFormatType
import ru.wb.go.utils.time.TimeFormatter

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

        return if (amount > 0) {
            CourierBillingPositiveItem(
                    date = dateFormat,
                    time = timeFormat,
                    amount = resourceProvider.formatMoney(amount, true),
                    idView = index
            )
        } else {
            val statusIcon = when (entity.statusOK) {
                StatusOK.IsComplete -> resourceProvider.iconIsComplete()
                StatusOK.IsProcessing -> resourceProvider.iconIsProcessing()
                StatusOK.IsRejected -> resourceProvider.iconIsRejected()
            }
            CourierBillingNegativeItem(
                    date = dateFormat,
                    time = timeFormat,
                    amount = resourceProvider.formatMoney(amount, false),
                    idView = index,
                    statusDescription = entity.statusDescription,
                    statusIcon = statusIcon
            )
        }
    }

}
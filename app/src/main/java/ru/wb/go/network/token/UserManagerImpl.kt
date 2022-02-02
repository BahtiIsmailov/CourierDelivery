package ru.wb.go.network.token

import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.utils.formatter.CarNumberUtils.MAX_NUMBER_DIGITS_MASK
import ru.wb.go.utils.prefs.SharedWorker

class UserManagerImpl(private val worker: SharedWorker) : UserManager {

    override fun isPhoneChanged(phone: String): Boolean {
        return worker.load(AppPreffsKeys.PHONE_KEY, "") != phone
    }

    override fun savePhone(phone: String) {
        worker.save(AppPreffsKeys.PHONE_KEY, phone)
    }

    override fun phone(): String {
        return worker.load(AppPreffsKeys.PHONE_KEY, "")
    }

    override fun saveCarNumber(number: String) {
        worker.save(AppPreffsKeys.CAR_NUMBER_KEY, number)
    }

    override fun carNumber(): String {
        return worker.load(AppPreffsKeys.CAR_NUMBER_KEY, MAX_NUMBER_DIGITS_MASK)
    }

    override fun savePaymentGuid(number: String) {
        worker.save(AppPreffsKeys.GUID_KEY, number)
    }

    override fun clearPaymentGuid() {
        savePaymentGuid("")
    }

    override fun getPaymentGuid(): String {
        return worker.load(AppPreffsKeys.GUID_KEY, "")
    }

    override fun clearAll() {
        worker.delete(AppPreffsKeys.PHONE_KEY)
        worker.delete(AppPreffsKeys.CAR_NUMBER_KEY)
    }

}
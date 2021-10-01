package ru.wb.perevozka.network.token

import ru.wb.perevozka.app.AppPreffsKeys
import ru.wb.perevozka.utils.prefs.SharedWorker

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
        return worker.load(AppPreffsKeys.CAR_NUMBER_KEY, "")
    }

    override fun saveStatusTask(status: String) {
        worker.save(AppPreffsKeys.STATUS_TASK_KEY, status)
    }

    override fun statusTask(): String {
        return worker.load(AppPreffsKeys.STATUS_TASK_KEY, "")
    }

    override fun clearAll() {
        worker.delete(AppPreffsKeys.PHONE_KEY)
        worker.delete(AppPreffsKeys.CAR_NUMBER_KEY)
        worker.delete(AppPreffsKeys.STATUS_TASK_KEY)
    }

    override fun clearStatus() {
        worker.delete(AppPreffsKeys.STATUS_TASK_KEY)
    }

}
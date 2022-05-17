package ru.wb.go.network.token

import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
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

    override fun saveCarType(type: Int) {
        worker.save(AppPreffsKeys.CAR_TYPE_KEY, type)
    }

    override fun carType(): Int {
        return worker.load(AppPreffsKeys.CAR_TYPE_KEY, -1)
    }

    override fun saveCarNumber(number: String) {
        worker.save(AppPreffsKeys.CAR_NUMBER_KEY, number)
    }

    override fun carNumber(): String {
        return worker.load(AppPreffsKeys.CAR_NUMBER_KEY, "")
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
        worker.delete(AppPreffsKeys.DEMO_OFF_KEY)
    }

    override fun saveCourierDocumentsEntity(courierDocumentsEntity: CourierDocumentsEntity) {
        worker.save(AppPreffsKeys.COURIER_DOCUMENTS_KEY, courierDocumentsEntity)
    }

    override fun courierDocumentsEntity(): CourierDocumentsEntity? {
        return worker.load(AppPreffsKeys.COURIER_DOCUMENTS_KEY, CourierDocumentsEntity::class.java)
    }

    override fun clearCourierDocumentsEntity() {
        worker.delete(AppPreffsKeys.COURIER_DOCUMENTS_KEY)
    }

}
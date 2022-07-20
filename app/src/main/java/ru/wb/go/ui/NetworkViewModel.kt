package ru.wb.go.ui

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity

abstract class NetworkViewModel() : ViewModel() {

    abstract fun getScreenTag(): String
    private val startLog = MutableLiveData<Boolean>()
    private val params = Bundle()


    fun logException(throwable: Throwable, message: String) {
        Firebase.crashlytics.log(message)
        Firebase.crashlytics.recordException(throwable)
    }

    fun logCourierAndOrderData(data: LocalOrderEntity) {
        params.putInt("wbUserID", data.wbUserID)
        params.putString("carNumber", data.carNumber)
        params.putInt("orderId", data.orderId)
        params.putInt("routeId", data.routeID)
        params.putString("gate", data.gate)
        params.putInt("minPrice", data.minPrice)
        params.putString("route", data.route)
        params.putInt("minVolume", data.minVolume)
        params.putInt("minBoxes", data.minBoxes)
        params.putInt("countOffices", data.countOffices)
        params.putString("reservedAt", data.reservedAt)
        params.putString("startedAt", data.startedAt)
        params.putString("reservedDuration", data.reservedDuration)
        params.putString("status", data.status)
        params.putString("srcName", data.srcName)
        params.putString("SrcAddress", data.srcAddress)
        params.putInt("cost", data.cost)
        params.putInt("srcId", data.srcId)
        params.putDouble("srcLongitude", data.srcLongitude)
        params.putDouble("srcLatitude", data.srcLatitude)

        if (startLog.value == true) {
            setDataToFireBase()
        }
    }

    fun setLogBoxesQrCodeAddressAndCount(boxesQrCode: String, address: String, boxesCount: String) {
        params.putString("boxesQrCode", boxesQrCode)
        params.putString("address", address)
        params.putString("boxesCount", boxesCount)
    }


    fun setValueToStartLog(startLog: Boolean) {
        this.startLog.value = startLog
    }

    private fun getValueStartLog() = startLog.value


    private fun setDataToFireBase() {
        Firebase.analytics.logEvent("CourierStartedOrder", params)
    }

}


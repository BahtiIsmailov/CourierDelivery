package ru.wb.go.ui

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.ColumnInfo
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.wb.go.db.entity.courierlocal.CourierTimerEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.ui.courierorders.CourierOrderDetailsInfoUIState

abstract class NetworkViewModel() : ViewModel() {

    abstract fun getScreenTag(): String
    private val countPvz = MutableLiveData<Int>()
    private val boxCount = MutableLiveData<Int>()
    private val params = Bundle()
    
     // fun onTechEventLog(method: String, message: String = EMPTY_MESSAGE) {
        //metric.//onTechEventLog(getScreenTag(), method, message)
    //}

     //fun onTechEventLog(method: String, error: Throwable) {
        //metric.//onTechEventLog(getScreenTag(), method, error.toString())
    //}

    fun logException(throwable: Throwable,message: String) {
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
        
        params.putInt("LoadingPvzCount",getLogPvzCount())
        params.putInt("LoadingBoxesCount",getBoxesCount())
        
        
    }
    
    fun setLogPvzCount(pvzCount:Int) {
        countPvz.value = pvzCount
    }       
    
    private fun getLogPvzCount() = countPvz.value?:0
    
    fun setLogBoxesCount(boxesCount:Int) {
        boxCount.value = boxesCount
    } 
    
    private fun getBoxesCount() = boxCount.value?:0
    
    
    fun setDataToFireBase(){
        Firebase.analytics.logEvent("CourierStartedOrder", params)
    }

}


package com.wb.logistics.ui.delivery.data

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.app.API_KEY
import com.wb.logistics.ui.delivery.data.delivery.Delivery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeliveryRepository(private val receptionApi: DeliveryApi, private val receptionDao: DeliveryDao) {

    val data = MutableLiveData<Delivery>()

    suspend fun refresh(city: String) {
        withContext(Dispatchers.IO) {
            val delivery = receptionApi.getTemplateAsync(city, API_KEY).await()
            data.postValue(delivery)
        }
    }

}
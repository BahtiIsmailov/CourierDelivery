package com.wb.logistics.ui.reception.data

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.app.API_KEY
import com.wb.logistics.ui.reception.data.delivery.Reception
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReceptionRepository(private val receptionApi: ReceptionApi, private val receptionDao: ReceptionDao) {

    val data = MutableLiveData<Reception>()

    suspend fun refresh(param: String) {
        withContext(Dispatchers.IO) {
            val reception = receptionApi.getTemplateAsync(param, API_KEY).await()
            data.postValue(reception)
        }
    }

}
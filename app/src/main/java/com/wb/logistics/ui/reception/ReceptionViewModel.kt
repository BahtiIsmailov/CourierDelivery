package com.wb.logistics.ui.reception

import androidx.lifecycle.ViewModel
import com.wb.logistics.ui.reception.data.ReceptionRepository
import com.wb.logistics.ui.res.AppResourceProvider

class ReceptionViewModel(
    private val receptionRepository: ReceptionRepository,
    private val resourceProvider: AppResourceProvider,
) : ViewModel() {

}
package com.wb.logistics.ui.reception

import androidx.lifecycle.ViewModel
import com.wb.logistics.ui.reception.data.ReceptionRepository
import com.wb.logistics.ui.res.ResourceProvider

class ReceptionViewModel(
    private val receptionRepository: ReceptionRepository,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

}
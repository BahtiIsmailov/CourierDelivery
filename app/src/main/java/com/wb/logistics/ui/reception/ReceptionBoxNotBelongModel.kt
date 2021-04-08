package com.wb.logistics.ui.reception

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReceptionBoxNotBelongModel(
    private val parameters: ReceptionBoxNotBelongParameters,
) : ViewModel() {

    private val _belongInfo = MutableLiveData<ReceptionBoxNotBelongState>()
    val belongInfo: LiveData<ReceptionBoxNotBelongState>
        get() = _belongInfo

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        _belongInfo.value =
            ReceptionBoxNotBelongState.BelongInfo(
                parameters.toolbarTitle,
                parameters.title,
                parameters.box,
                parameters.address)
    }

    fun onUnderstandClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}
package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UnloadingBoxNotBelongModel(
    private val parameters: UnloadingBoxNotBelongParameters,
) : ViewModel() {

    private val _belongInfo = MutableLiveData<UnloadingBoxNotBelongState>()
    val belongInfo: LiveData<UnloadingBoxNotBelongState>
        get() = _belongInfo

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        _belongInfo.value =
            UnloadingBoxNotBelongState.BelongInfo(
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
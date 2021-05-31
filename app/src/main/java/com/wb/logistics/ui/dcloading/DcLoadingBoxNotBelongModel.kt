package com.wb.logistics.ui.dcloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DcLoadingBoxNotBelongModel(
    private val parameters: DcLoadingBoxNotBelongParameters,
) : ViewModel() {

    private val _belongInfo = MutableLiveData<DcLoadingBoxNotBelongState>()
    val belongInfo: LiveData<DcLoadingBoxNotBelongState>
        get() = _belongInfo

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        _belongInfo.value =
            DcLoadingBoxNotBelongState.BelongInfo(
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
package ru.wb.perevozka.ui.courierloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CourierScannerLoadingBoxNotBelongViewModel(parameters: DcLoadingBoxNotBelongParameters) : ViewModel() {

    private val _belongInfo = MutableLiveData<CourierScannerLoadingBoxNotBelongState>()
    val belongInfo: LiveData<CourierScannerLoadingBoxNotBelongState>
        get() = _belongInfo

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        _belongInfo.value =
            with(parameters) {
                CourierScannerLoadingBoxNotBelongState.BelongInfo(
                    title,
                    box,
                    address,
                    (address.isNotEmpty())
                )
            }
    }

    fun onUnderstandClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}
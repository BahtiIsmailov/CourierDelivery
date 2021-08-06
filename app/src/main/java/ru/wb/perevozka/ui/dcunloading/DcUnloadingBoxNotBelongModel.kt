package ru.wb.perevozka.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DcUnloadingBoxNotBelongModel(
    private val parameters: DcUnloadingBoxNotBelongParameters,
) : ViewModel() {

    private val _belongInfo = MutableLiveData<DcUnloadingBoxNotBelongState>()
    val belongInfo: LiveData<DcUnloadingBoxNotBelongState>
        get() = _belongInfo

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        _belongInfo.value =
            DcUnloadingBoxNotBelongState.BelongInfo(parameters.toolbarTitle)
    }

    fun onUnderstandClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}
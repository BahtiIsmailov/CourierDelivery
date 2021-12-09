package ru.wb.go.ui.unloadingscan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UnloadingBoxNotBelongModel(
    parameters: UnloadingBoxNotBelongParameters,
) : ViewModel() {

    private val _belongInfo = MutableLiveData<UnloadingBoxNotBelongState>()
    val belongInfo: LiveData<UnloadingBoxNotBelongState>
        get() = _belongInfo

    private val _navigateToBack = MutableLiveData<NavigateToBack>()
    val navigateToBack: LiveData<NavigateToBack>
        get() = _navigateToBack

    init {
        _belongInfo.value =
            with(parameters) {
                UnloadingBoxNotBelongState.BelongInfo(
                    title,
                    description,
                    box,
                    address,
                    address.isNotEmpty())
            }
    }

    fun onUnderstandClick() {
        _navigateToBack.value = NavigateToBack
    }

    object NavigateToBack

}
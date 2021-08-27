package ru.wb.perevozka.ui.courierloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.ui.NetworkViewModel

class CourierLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navigationState = MutableLiveData<CourierLoaderNavigationState>()
    val navigationState: LiveData<CourierLoaderNavigationState>
        get() = _navigationState

    init {
        checkUserState()
    }

    private fun checkUserState() {
        val phone = tokenManager.userPhone()
        when {
            tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) -> toUserForm(phone)
            tokenManager.resources()
                .contains(NEED_APPROVE_COURIER_DOCUMENTS) -> toCouriersCompleteRegistration(
                phone
            )
            else -> toCourierWarehouse()
        }
    }

    private fun toCourierWarehouse() {
        _navigationState.value = CourierLoaderNavigationState.NavigateToCourierWarehouse
    }

    private fun toUserForm(phone: String) {
        _navigationState.value = CourierLoaderNavigationState.NavigateToCourierUserForm(phone)
    }

    private fun toCouriersCompleteRegistration(phone: String) {
        _navigationState.value =
            CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration(phone)
    }

}
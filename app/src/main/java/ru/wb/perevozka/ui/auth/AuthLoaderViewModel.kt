package ru.wb.perevozka.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.ui.NetworkViewModel

class AuthLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navState = MutableLiveData<AuthLoaderUINavState>()
    val navState: LiveData<AuthLoaderUINavState>
        get() = _navState

    init {
        checkUserState()
    }

    private fun checkUserState() {
        if (tokenManager.isContains()) {
            val phone = tokenManager.userPhone()
            when {
                tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) -> toUserForm(phone)
                tokenManager.resources()
                    .contains(NEED_APPROVE_COURIER_DOCUMENTS) -> toCouriersCompleteRegistration(
                    phone
                )
            }
        } else toNumberPhone()
    }

    private fun toNumberPhone() {
        _navState.value = AuthLoaderUINavState.NavigateToNumberPhone
    }

    private fun toUserForm(phone: String) {
        _navState.value = AuthLoaderUINavState.NavigateToUserForm(phone)
    }

    private fun toCouriersCompleteRegistration(phone: String) {
        _navState.value = AuthLoaderUINavState.NavigateToCouriersCompleteRegistration(phone)
    }

}
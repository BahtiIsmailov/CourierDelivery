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
) : NetworkViewModel(compositeDisposable) {

    private val _navigationState = MutableLiveData<AuthLoaderNavigationState>()
    val navigationState: LiveData<AuthLoaderNavigationState>
        get() = _navigationState

    init {
        checkUserState()
    }

    private fun checkUserState() {
        toNumberPhone()
    }

    private fun toNumberPhone() {
        _navigationState.value = AuthLoaderNavigationState.NavigateToNumberPhone
    }

}
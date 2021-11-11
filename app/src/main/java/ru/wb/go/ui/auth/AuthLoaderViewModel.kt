package ru.wb.go.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.NetworkViewModel

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
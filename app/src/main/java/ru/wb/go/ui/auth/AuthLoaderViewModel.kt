package ru.wb.go.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
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
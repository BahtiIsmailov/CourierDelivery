package ru.wb.go.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.ui.NetworkViewModel

class AuthLoaderViewModel(
) : NetworkViewModel() {

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

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "AuthLoader"
    }



}
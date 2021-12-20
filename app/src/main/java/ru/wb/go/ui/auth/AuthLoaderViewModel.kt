package ru.wb.go.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.ui.NetworkViewModel

class AuthLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
) : NetworkViewModel(compositeDisposable, metric) {

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
        onTechEventLog("toNumberPhone")
        _navigationState.value = AuthLoaderNavigationState.NavigateToNumberPhone
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "AuthLoader"
    }

}
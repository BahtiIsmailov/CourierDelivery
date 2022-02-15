package ru.wb.go.ui.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.analytics.YandexMetricManager

class AppLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _navState = MutableLiveData<AppLoaderNavigatioState>()
    val navState: LiveData<AppLoaderNavigatioState>
        get() = _navState

    private val _demoState = MutableLiveData<Boolean>()
    val demoState: LiveData<Boolean>
        get() = _demoState

    init {
        selectStateApp()
    }

    private fun selectStateApp() {
        if (tokenManager.isUserCourier()) toCourier()
        else {
            if (tokenManager.isDemo()) _demoState.value = true
            else toAuth()
        }
    }

    private fun toCourier() {
        _navState.value = AppLoaderNavigatioState.NavigateToCourier
    }

    private fun toAuth() {
        _navState.value = AppLoaderNavigatioState.NavigateToAuth
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    fun toRegistrationClick() {
        toAuth()
    }

    fun toDemoClick() {
        toCourier()
    }

    companion object {
        const val SCREEN_TAG = "AppLoader"
    }

}
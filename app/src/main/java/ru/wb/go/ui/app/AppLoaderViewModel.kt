package ru.wb.go.ui.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.headers.RefreshTokenRepository
import ru.wb.go.network.rx.RxSchedulerFactory
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

    init {
        selectStateApp()
    }

    private fun selectStateApp() {
        if (tokenManager.isUserCourier()) {
            _navState.value = AppLoaderNavigatioState.NavigateToCourier
        } else _navState.value = AppLoaderNavigatioState.NavigateToAuth
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "AppLoader"
    }

}
package ru.wb.go.ui.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.COURIER_COMPANY_ID
import ru.wb.go.app.COURIER_ROLE
import ru.wb.go.network.headers.RefreshTokenRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.SettingsManager
import java.net.UnknownHostException

class AppLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val repository: RefreshTokenRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _navState = MutableLiveData<AppLoaderNavigatioState>()
    val navState: LiveData<AppLoaderNavigatioState>
        get() = _navState

    init {
        refreshTokenAndNavigateToApp()
    }

    private fun refreshTokenAndNavigateToApp() {
        addSubscription(repository.refreshAccessTokensSync()
            .compose(rxSchedulerFactory.applyCompletableSchedulers()).subscribe(
                {
                    LogUtils { logDebugApp("refreshTokenAndNavigateToApp complete") }
                    if (tokenManager.isContains()) {
                        if (tokenManager.userCompanyId() == COURIER_COMPANY_ID
                            || tokenManager.resources().contains(COURIER_ROLE)
                        ) toCourier()
                        else toCourier()
                    } else toAuth()
                },
                {
                    LogUtils { logDebugApp("refreshTokenAndNavigateToApp error " + it) }
                    if (it is UnknownHostException) {
                        if (tokenManager.isContains()) {
                            if (tokenManager.userCompanyId() == COURIER_COMPANY_ID
                                || tokenManager.resources().contains(COURIER_ROLE)
                            ) toCourier()
                            else toAuth()
                        } else toAuth()
                    } else {
                        toAuth()
                    }
                }
            ))
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

    companion object {
        const val SCREEN_TAG = "AppLoader"
    }

}
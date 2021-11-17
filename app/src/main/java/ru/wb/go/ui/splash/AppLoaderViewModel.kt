package ru.wb.go.ui.splash

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
import java.net.UnknownHostException

class AppLoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val repository: RefreshTokenRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable) {

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
                        else toDelivery()
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

    private fun toDelivery() {
        _navState.value = AppLoaderNavigatioState.NavigateToDelivery
    }

    private fun toCourier() {
        _navState.value = AppLoaderNavigatioState.NavigateToCourier
    }

    private fun toAuth() {
        _navState.value = AppLoaderNavigatioState.NavigateToAuth
    }

}
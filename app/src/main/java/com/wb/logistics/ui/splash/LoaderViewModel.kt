package com.wb.logistics.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.headers.RefreshTokenRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.network.token.TokenManager
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.splash.domain.ScreenManager
import io.reactivex.disposables.CompositeDisposable

class LoaderViewModel(
    compositeDisposable: CompositeDisposable,
    repository: RefreshTokenRepository,
    rxSchedulerFactory: RxSchedulerFactory,
    tokenManager: TokenManager,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navState = MutableLiveData<LoaderUINavState>()
    val navState: LiveData<LoaderUINavState>
        get() = _navState

    init {
        if (tokenManager.isContains()) {
            addSubscription(repository.refreshAccessTokensSync()
                .compose(rxSchedulerFactory.applyCompletableSchedulers()).subscribe({
                    toApp()
                }, {
                    toNumberPhone()
                }))
        } else {
            toNumberPhone()
        }
    }

    private fun toApp() {
        _navState.value = LoaderUINavState.NavigateToApp
    }

    private fun toNumberPhone() {
        _navState.value = LoaderUINavState.NavigateToNumberPhone
    }

}
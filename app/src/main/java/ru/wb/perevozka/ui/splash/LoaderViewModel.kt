package ru.wb.perevozka.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.perevozka.network.headers.RefreshTokenRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.utils.managers.ScreenManager
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
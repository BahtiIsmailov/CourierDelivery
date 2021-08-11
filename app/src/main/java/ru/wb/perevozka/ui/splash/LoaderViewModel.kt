package ru.wb.perevozka.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.perevozka.network.headers.RefreshTokenRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.utils.managers.ScreenManager

class LoaderViewModel(
    compositeDisposable: CompositeDisposable,
    private val repository: RefreshTokenRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val tokenManager: TokenManager,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _navState = MutableLiveData<LoaderUINavState>()
    val navState: LiveData<LoaderUINavState>
        get() = _navState

    init {
        refreshTokenAndNavigateToApp()
    }

    private fun refreshTokenAndNavigateToApp() {
        addSubscription(repository.refreshAccessTokensSync()
            .compose(rxSchedulerFactory.applyCompletableSchedulers()).subscribe(
                {
                    if (!tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS)
                        && !tokenManager.resources().contains(NEED_APPROVE_COURIER_DOCUMENTS)
                    ) toApp()
                    else toNumberPhone()
                },
                { toNumberPhone() }
            ))
    }

    private fun toApp() {
        _navState.value = LoaderUINavState.NavigateToApp
    }

    private fun toNumberPhone() {
        _navState.value = LoaderUINavState.NavigateToNumberPhone
    }

}
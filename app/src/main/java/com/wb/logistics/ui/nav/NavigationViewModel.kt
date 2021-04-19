package com.wb.logistics.ui.nav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.nav.domain.NavigationInteractor
import com.wb.logistics.ui.nav.domain.ScreenManager
import com.wb.logistics.ui.nav.domain.ScreenState
import com.wb.logistics.ui.res.AppResourceProvider
import com.wb.logistics.utils.managers.DeviceManager
import io.reactivex.disposables.CompositeDisposable

class NavigationViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: NavigationInteractor,
    private val resourceProvider: AppResourceProvider,
    private val deviceManager: DeviceManager,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = MutableLiveData<NavigationUINavState>()

    private val _backTtn = MutableLiveData<Boolean>()
    val backTtn: LiveData<Boolean>
        get() = _backTtn

    private val _navHeader = MutableLiveData<Pair<String, String>>()
    val navHeader: LiveData<Pair<String, String>>
        get() = _navHeader

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _networkState = MutableLiveData<Boolean>()
    val networkState: LiveData<Boolean>
        get() = _networkState

    init {
        fetchNavHeader()
        fetchVersionApp()
        fetchNetworkState()

        when (screenManager.readScreenState()) {
            ScreenState.FLIGHT -> {
            }
            ScreenState.RECEPTION_SCAN -> stateUINav.value =
                NavigationUINavState.NavigateToReceptionScan
            ScreenState.FLIGHT_PICK_UP_POINT -> stateUINav.value =
                    NavigationUINavState.NavigateToPickUpPoint
            ScreenState.FLIGHT_DELIVERY -> stateUINav.value =
                NavigationUINavState.NavigateToDelivery
        }
    }

    private fun fetchNavHeader() {
        addSubscription(interactor.sessionInfo().subscribe({ _navHeader.value = it }, {}))
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun fetchNetworkState() {
        addSubscription(interactor.isNetworkConnected().subscribe({ _networkState.value = it }, {}))
    }

    fun onChangeTitle() {
        if (screenManager.readScreenState() == ScreenState.FLIGHT_DELIVERY) _backTtn.value = true
    }

}
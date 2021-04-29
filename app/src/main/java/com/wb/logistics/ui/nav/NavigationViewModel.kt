package com.wb.logistics.ui.nav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.nav.domain.NavigationInteractor
import com.wb.logistics.ui.nav.domain.ScreenManager
import com.wb.logistics.ui.nav.domain.ScreenManagerState
import com.wb.logistics.utils.managers.DeviceManager
import io.reactivex.disposables.CompositeDisposable

class NavigationViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: NavigationInteractor,
    private val resourceProvider: NavigationResourceProvider,
    private val deviceManager: DeviceManager,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    val stateUINav = MutableLiveData<NavigationNavAction>()

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

        when (val state = screenManager.readScreenState()) {
            is ScreenManagerState.Flight -> {
            }
            is ScreenManagerState.ReceptionScan -> stateUINav.value =
                NavigationNavAction.NavigateToReceptionScan
            is ScreenManagerState.FlightPickUpPoint -> stateUINav.value =
                NavigationNavAction.NavigateToPickUpPoint
            is ScreenManagerState.FlightDelivery -> stateUINav.value =
                NavigationNavAction.NavigateToDelivery
            is ScreenManagerState.Unloading -> stateUINav.value =
                NavigationNavAction.NavigateToUnloading(state.officeId, state.shortAddress)
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

}
package com.wb.logistics.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.splash.domain.AppInteractor
import com.wb.logistics.utils.managers.DeviceManager
import io.reactivex.disposables.CompositeDisposable

class AppViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: AppInteractor,
    private val resourceProvider: AppResourceProvider,
    private val deviceManager: DeviceManager

    ) : NetworkViewModel(compositeDisposable) {

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
        fetchNetworkState()
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

    fun onExitClick() {
        interactor.exitAuth()
    }

    fun updateDrawer() {
        fetchNavHeader()
        fetchVersionApp()
    }

}
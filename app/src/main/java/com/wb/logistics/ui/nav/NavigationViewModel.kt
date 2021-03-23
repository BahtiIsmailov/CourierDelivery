package com.wb.logistics.ui.nav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.nav.domain.NavigationInteractor
import com.wb.logistics.ui.res.AppResourceProvider
import com.wb.logistics.utils.managers.DeviceManager
import io.reactivex.disposables.CompositeDisposable

class NavigationViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: NavigationInteractor,
    private val resourceProvider: AppResourceProvider,
    private val deviceManager: DeviceManager
) : NetworkViewModel(compositeDisposable) {

    private val _navHeader = MutableLiveData<Pair<String, String>>()
    val navHeader: LiveData<Pair<String, String>>
        get() = _navHeader

    private val _countFlight = MutableLiveData<String>()
    val countFlight: LiveData<String>
        get() = _countFlight

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    init {
        fetchNavHeader()
        fetchCountFlight()
        fetchVersionApp()
    }

    private fun fetchNavHeader() {
        addSubscription(interactor.sessionInfo().subscribe({ _navHeader.value = it }, {}))
    }

    private fun fetchCountFlight() {
        _countFlight.value = "1"
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

}
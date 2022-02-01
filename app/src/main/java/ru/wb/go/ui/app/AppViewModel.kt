package ru.wb.go.ui.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.auth.AppVersionState
import ru.wb.go.ui.app.domain.AppInteractor
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager

class AppViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: AppInteractor,
    private val resourceProvider: AppResourceProvider,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    init {
        fetchNetworkState()
        fetchVersionApp()
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

    private fun fetchNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _networkState.value = it }, {})
        )
    }

    fun onExitClick() {
        interactor.exitAuth()
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "App"
    }

}
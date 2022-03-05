package ru.wb.go.ui.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.app.domain.AppInteractor
import ru.wb.go.ui.app.domain.AppNavRepositoryImpl.Companion.INVALID_TOKEN
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.SettingsManager

class AppViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val interactor: AppInteractor,
    private val resourceProvider: AppResourceProvider,
    private val deviceManager: DeviceManager,
    private val settingsManager: SettingsManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigation = MutableLiveData<NavigateToRegistration>()
    val navigation: LiveData<NavigateToRegistration>
        get() = _navigation

    init {
        fetchNetworkState()
        fetchVersionApp()

        addSubscription(
            interactor.observeNavigationApp()
                .subscribe({
                    if (it == INVALID_TOKEN) {
                        interactor.exitAuth()
                        _navigation.value = NavigateToRegistration
                    } else
                        throw IllegalStateException("Wrong param $it")
                }, {})
        )
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
        _navigation.value = NavigateToRegistration
    }

    fun getDarkThemeSetting(): Boolean {
        return settingsManager.getSetting(
            AppPreffsKeys.SETTING_THEME,
            false
        )
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "App"
    }

    object NavigateToRegistration

}
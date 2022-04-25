package ru.wb.go.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.app.AppViewModel
import ru.wb.go.ui.app.domain.AppInteractor
import ru.wb.go.ui.settings.domain.SettingsInteractor
import ru.wb.go.ui.settings.domain.SettingsResourceProvider
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.SettingsManager

class SettingsViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourcesProvider: SettingsResourceProvider,
    private val deviceManager: DeviceManager,
    private val interactor: SettingsInteractor,
    private val settingsManager: SettingsManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    init {
        observeNetworkState()
        fetchVersionApp()
    }

    fun getSetting(setting: String, default: Boolean): Boolean {
        return settingsManager.getSetting(setting, default)
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourcesProvider.getVersionApp(deviceManager.toolbarVersion)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    fun settingClick(setting: String, state: Boolean) {
        settingsManager.setSetting(setting, state)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "Settings"
    }

    object NavigateToWarehouse

}
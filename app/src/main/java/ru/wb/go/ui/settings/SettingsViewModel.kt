package ru.wb.go.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.settings.domain.SettingsInteractor
import ru.wb.go.ui.settings.domain.SettingsResourceProvider
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.SettingsManager

class SettingsViewModel(
    private val resourcesProvider: SettingsResourceProvider,
    private val deviceManager: DeviceManager,
    private val interactor: SettingsInteractor,
    private val settingsManager: SettingsManager,
) : NetworkViewModel() {

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
        _versionApp.value =  resourcesProvider.getVersionApp(deviceManager.toolbarVersion)
    }

    private fun observeNetworkState() {
        interactor.observeNetworkConnected()
            .onEach {
                _toolbarNetworkState.value = it
            }
            .catch {
                logException(it,"observeNetworkState")
            }
            .launchIn(viewModelScope)
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


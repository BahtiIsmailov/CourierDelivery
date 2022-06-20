package ru.wb.go.ui.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    private val _navigation = MutableLiveData<NavigationState>()
    val navigation: LiveData<NavigationState>
        get() = _navigation

    init {
        fetchNetworkState()
        fetchVersionApp()
        observeNavigationApp()

    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }


    private fun observeNavigationApp(){
        interactor.observeNavigationApp()
            .onEach {
                if (it == INVALID_TOKEN) {
                    interactor.exitAuth()
                    _navigation.value = NavigationState.NavigateToRegistration
                } else
                    throw IllegalStateException("Wrong param $it")
            }
            .launchIn(viewModelScope)
    }


    private fun fetchNetworkState() {
        interactor.observeNetworkConnected()
            .onEach {
                _networkState.value = it
            }
            .catch {  }
            .launchIn(viewModelScope)
    }

    fun onSupportClick() {
        _navigation.value = NavigationState.NavigateToSupport
    }

    fun onExitAuthClick() {
        viewModelScope.launch {
            interactor.exitAuth()
            _navigation.value = NavigationState.NavigateToRegistration
        }
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


    sealed class NavigationState {

        object NavigateToRegistration : NavigationState()

        object NavigateToSupport : NavigationState()

    }



}
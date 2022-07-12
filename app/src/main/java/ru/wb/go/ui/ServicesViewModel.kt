package ru.wb.go.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.wb.go.mvvm.BaseServicesResourceProvider
import ru.wb.go.network.monitor.NetworkState

abstract class ServicesViewModel(
    private val serviceInteractor: BaseServiceInteractor,
    private val resourceProvider: BaseServicesResourceProvider
) : NetworkViewModel() {

    private val _networkState = MutableLiveData<NetworkState>()
    val networkState: LiveData<NetworkState>
        get() = _networkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    init {
        observeNetworkState()
        fetchVersionApp()
    }

    private fun observeNetworkState() {
        serviceInteractor.observeNetworkConnected()
            .onEach {
                _networkState.value = it
            }
            .catch {  }
            .launchIn(viewModelScope)
    }



    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(serviceInteractor.versionApp())
    }

}


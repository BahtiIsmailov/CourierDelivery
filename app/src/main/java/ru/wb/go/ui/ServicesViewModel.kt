package ru.wb.go.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.wb.go.mvvm.BaseServicesResourceProvider
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.utils.analytics.YandexMetricManager

abstract class ServicesViewModel(
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val serviceInteractor: BaseServiceInteractor,
    private val resourceProvider: BaseServicesResourceProvider
) : NetworkViewModel(compositeDisposable, metric) {

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

//    private fun observeNetworkState() {
//        addSubscription(
//            serviceInteractor.observeNetworkConnected()
//                .subscribe({ _networkState.postValue(it) }, {})
//        )
//    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(serviceInteractor.versionApp())
    }

}


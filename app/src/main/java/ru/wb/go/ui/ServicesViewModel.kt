package ru.wb.go.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
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
        addSubscription(
            serviceInteractor.observeNetworkConnected()
                .subscribe({ _networkState.value = it }, {})
        )
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(serviceInteractor.versionApp())
    }

}


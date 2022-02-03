package ru.wb.go.ui.courierunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.courierunloading.domain.*
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.DeviceManager


class RemainBoxViewModel(
    private val parameters: RemainBoxParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierUnloadingResourceProvider,
    private val interactor: CourierUnloadingInteractor,
    private val deviceManager: DeviceManager,
) : NetworkViewModel(compositeDisposable, metric) {
    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _boxes = MutableLiveData<RemainBoxItemState>()
    val boxes: LiveData<RemainBoxItemState>
        get() = _boxes

    private var boxItems = mutableListOf<String>()

    init {
        fetchVersionApp()
        observeNetworkState()
        initBoxes()
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.toolbarVersion)
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected()
                .subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    private fun initBoxes() {
        addSubscription(
            interactor.getRemainBoxes(parameters.officeId)
                .subscribe(
                    { fillRemainBoxList(it) },
                    { })
        )
    }

    private fun fillRemainBoxList(boxes: List<LocalBoxEntity>) {
        boxItems = boxes.map {
            "*".repeat(6) +" "+ it.boxId.padStart(3,'0').takeLast(3)
        }.toMutableList()
        _boxes.value =
            if (boxItems.isEmpty()) RemainBoxItemState.Empty("")
            else RemainBoxItemState.InitItems(boxItems)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "RemainBox"
    }

    data class Label(val label: String)
}

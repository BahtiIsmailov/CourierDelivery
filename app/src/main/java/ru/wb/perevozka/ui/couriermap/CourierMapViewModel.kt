package ru.wb.perevozka.ui.couriermap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.couriermap.domain.CourierMapInteractor

class CourierMapViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: CourierMapInteractor,
    private val resourceProvider: CourierMapResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _mapState = MutableLiveData<CourierMapState>()
    val mapState: LiveData<CourierMapState>
        get() = _mapState

    init {
        subscribeMapState()
    }

    private fun subscribeMapState() {
        addSubscription(
            interactor.subscribeMapState().subscribe({ _mapState.value = it }, {})
        )
    }

    fun onInitPermission() {
        interactor.onInitPermission()
    }

    fun onItemClick(index: String) {
        interactor.onItemClick(index)
    }

}
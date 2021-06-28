package com.wb.logistics.ui.unloadingcongratulation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.unloadingcongratulation.domain.CongratulationInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class CongratulationViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CongratulationResourceProvider,
    private val interactor: CongratulationInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<String>()
    val infoState: LiveData<String>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToDcUnload>()
    val navigateToBack: LiveData<NavigateToDcUnload>
        get() = _navigateToBack

    init {
        addSubscription(interactor.getDeliveryBoxesGroupByOffice()
            .flatMap { boxes ->
                Observable.fromIterable(boxes)
                    .map { DeliveryResult(it.unloadedCount, it.attachedCount) }
                    .reduce(DeliveryResult(0, 0),
                        { accumulator, item ->
                            val attachedCount = accumulator.attachedCount + item.attachedCount
                            val unloadedCount = accumulator.unloadedCount + item.unloadedCount
                            DeliveryResult(unloadedCount, attachedCount)
                        })
            }
            .map {
                with(it) {
                    resourceProvider.getInfo(unloadedCount, attachedCount + unloadedCount)
                }
            }
            .subscribe({ _infoState.value = it }) {})
    }

    fun onCompleteClick() {
        _navigateToBack.value = NavigateToDcUnload
    }

    object NavigateToDcUnload

    data class DeliveryResult(val unloadedCount: Int, val attachedCount: Int)

}
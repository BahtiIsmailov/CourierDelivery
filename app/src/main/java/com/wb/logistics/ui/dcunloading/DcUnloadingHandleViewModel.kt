package com.wb.logistics.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.dcunloading.domain.DcUnloadingInteractor
import com.wb.logistics.utils.formatter.BoxUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class DcUnloadingHandleViewModel(
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
    interactor: DcUnloadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUI = MutableLiveData<DcUnloadingHandleUIState<String>>()
    val stateUI: LiveData<DcUnloadingHandleUIState<String>>
        get() = _stateUI

    init {
        addSubscription(interactor.findDcUnloadedHandleBoxes()
            .flatMap {
                Observable.fromIterable(it.withIndex())
                    .map { box ->
                        "" + (box.index + 1) + ". " + box.value.barcode.take(4) + "....." + box.value.barcode.takeLast(
                            4)
                    }.toList()
            }
            .subscribe({
                _stateUI.value = if (it.isEmpty()) DcUnloadingHandleUIState.BoxesEmpty
                else DcUnloadingHandleUIState.BoxesComplete(it)
            }, {}))
    }

    fun action(actionView: DcUnloadingHandleUIAction) {
        when (actionView) {
            is DcUnloadingHandleUIAction.BoxChanges -> {
                fetchBoxNumberFormat(actionView)
            }
        }
    }

    private fun fetchBoxNumberFormat(actionView: DcUnloadingHandleUIAction.BoxChanges) {
        addSubscription(
            BoxUtils.boxNumberFormatter(actionView.observable, rxSchedulerFactory)
                .map {
                    if (it.length > 5) DcUnloadingHandleUIState.BoxFormatted(it)
                    else DcUnloadingHandleUIState.BoxAcceptDisabled(it)
                }
                .subscribe { _stateUI.value = it }
        )
    }

}
package com.wb.logistics.ui.unloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.unloading.domain.UnloadingInteractor
import com.wb.logistics.utils.formatter.BoxUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class UnloadingHandleViewModel(
    parameters: UnloadingHandleParameters,
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
    interactor: UnloadingInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUI = MutableLiveData<UnloadingHandleUIState<String>>()
    val stateUI: LiveData<UnloadingHandleUIState<String>>
        get() = _stateUI

    init {
        addSubscription(interactor.observeAttachedBoxes(parameters.dstOfficeId)
            .switchMap {
                Observable.fromIterable(it.withIndex())
                    .map {
                        "" + (it.index + 1) + ". " + it.value.barcode.take(4) + "....." + it.value.barcode.takeLast(
                            4)
                    }.toList().toObservable()
            }
            .subscribe {
                _stateUI.value = if (it.isEmpty()) UnloadingHandleUIState.BoxesEmpty
                else UnloadingHandleUIState.BoxesComplete(it)
            })
    }

    fun action(actionView: UnloadingHandleUIAction) {
        when (actionView) {
            is UnloadingHandleUIAction.BoxChanges -> {
                fetchBoxNumberFormat(actionView)
            }
        }
    }


    private fun fetchBoxNumberFormat(actionView: UnloadingHandleUIAction.BoxChanges) {
        addSubscription(
            BoxUtils.boxNumberFormatter(actionView.observable, rxSchedulerFactory)
                .map {
                    if (it.length > 5) UnloadingHandleUIState.BoxFormatted(it) else UnloadingHandleUIState.BoxAcceptDisabled(
                        it)
                }
                .subscribe { _stateUI.value = it }
        )
    }

}
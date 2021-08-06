package ru.wb.perevozka.ui.dcunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.dcunloading.domain.DcUnloadingInteractor
import ru.wb.perevozka.utils.formatter.BoxUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class DcUnloadingHandleViewModel(
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
    interactor: DcUnloadingInteractor,
    resourceProvider: DcUnloadingScanResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUI = MutableLiveData<DcUnloadingHandleUIState<String>>()
    val stateUI: LiveData<DcUnloadingHandleUIState<String>>
        get() = _stateUI

    init {
        addSubscription(interactor.findDcUnloadedHandleBoxes()
            .flatMap {
                Observable.fromIterable(it.withIndex())
                    .map { box ->
                        resourceProvider.getUnnamedBarcodeFormat(box.index + 1, box.value.barcode)
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
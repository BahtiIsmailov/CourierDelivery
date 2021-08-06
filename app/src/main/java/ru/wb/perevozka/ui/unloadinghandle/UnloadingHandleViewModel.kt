package ru.wb.perevozka.ui.unloadinghandle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.unloadinghandle.domain.UnloadingHandleInteractor
import ru.wb.perevozka.ui.unloadingscan.UnloadingScanResourceProvider
import ru.wb.perevozka.utils.formatter.BoxUtils
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class UnloadingHandleViewModel(
    parameters: UnloadingHandleParameters,
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
    interactor: UnloadingHandleInteractor,
    resourceProvider: UnloadingScanResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _stateUI = MutableLiveData<UnloadingHandleUIState>()
    val stateUI: LiveData<UnloadingHandleUIState>
        get() = _stateUI

    init {
        addSubscription(interactor.observeAttachedBoxes(parameters.dstOfficeId)
//            .map {
//                val list = mutableListOf<FlightBoxEntity>()
//                for (i in 0..20) {
//                    list.add(it[0])
//                }
//                return@map list
//            }
            .switchMap {
                Observable.fromIterable(it.withIndex())
                    .map { box ->
                        with(box) {
                            resourceProvider.getHandleFormatBox(index + 1,
                                value.barcode.take(4),
                                value.barcode.takeLast(4))
                        }
                    }
                    .toList()
                    .toObservable()
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
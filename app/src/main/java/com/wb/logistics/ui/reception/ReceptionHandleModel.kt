package com.wb.logistics.ui.reception

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.network.rx.RxSchedulerFactory
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.utils.formatter.BoxUtils
import io.reactivex.disposables.CompositeDisposable

class ReceptionHandleModel(
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<ReceptionHandleUIState<String>>()

    fun action(actionView: ReceptionHandleUIAction) {
        when (actionView) {
            is ReceptionHandleUIAction.BoxChanges -> {
                fetchBoxNumberFormat(actionView)
            }
        }
    }

    private fun fetchBoxNumberFormat(actionView: ReceptionHandleUIAction.BoxChanges) {
        addSubscription(
            BoxUtils.boxNumberFormatter(actionView.observable, rxSchedulerFactory)
                .subscribe { number ->
                    stateUI.value =  ReceptionHandleUIState.BoxFormatted(number)
                })
    }

}
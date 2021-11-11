package ru.wb.go.ui.dcloading

import androidx.lifecycle.MutableLiveData
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.formatter.BoxUtils
import io.reactivex.disposables.CompositeDisposable

class DcLoadingHandleViewModel(
    compositeDisposable: CompositeDisposable,
    private val rxSchedulerFactory: RxSchedulerFactory,
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<DcLoadingHandleUIState>()

    fun action(actionView: DcLoadingHandleUIAction) {
        when (actionView) {
            is DcLoadingHandleUIAction.BoxChanges -> {
                fetchBoxNumberFormat(actionView)
            }
        }
    }

    private fun fetchBoxNumberFormat(actionView: DcLoadingHandleUIAction.BoxChanges) {
        addSubscription(
            BoxUtils.boxNumberFormatter(actionView.observable, rxSchedulerFactory)
                .map {
                    if (it.length > 5) DcLoadingHandleUIState.BoxFormatted(it)
                    else DcLoadingHandleUIState.BoxAcceptDisabled(it)
                }
                .subscribe { stateUI.value = it }
        )
    }

}
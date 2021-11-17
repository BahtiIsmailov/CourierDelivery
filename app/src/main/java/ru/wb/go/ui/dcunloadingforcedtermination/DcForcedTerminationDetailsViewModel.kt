package ru.wb.go.ui.dcunloadingforcedtermination

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.dcunloadingforcedtermination.domain.DcForcedTerminationInteractor
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable

class DcForcedTerminationDetailsViewModel(
    compositeDisposable: CompositeDisposable,
    private val interactor: DcForcedTerminationInteractor,
    private val dataBuilder: DcForcedTerminationDetailsDataBuilder,
) : NetworkViewModel(compositeDisposable) {

    private val _boxesState = MutableLiveData<DcForcedTerminationDetailsState>()
    val boxesState: LiveData<DcForcedTerminationDetailsState>
        get() = _boxesState

    private val _navigateToBack = MutableLiveData<DcForcedTerminationDetailsNavAction>()
    val navigateToBack: LiveData<DcForcedTerminationDetailsNavAction>
        get() = _navigateToBack

    init {
        addSubscription(interactor.notDcUnloadedBoxes()
            .flatMap { list ->
                Observable.fromIterable(list.withIndex())
                    .map { dataBuilder.buildDcForcedTerminationItem(it) }.toList()
            }
            .subscribe({
                _boxesState.value = DcForcedTerminationDetailsState.BoxesComplete(it)
            },
                {}))
    }


    fun onCompleteClick() {
        _navigateToBack.value = DcForcedTerminationDetailsNavAction.NavigateToBack
    }


}
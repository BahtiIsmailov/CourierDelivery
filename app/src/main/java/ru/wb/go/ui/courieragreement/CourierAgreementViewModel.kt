package ru.wb.go.ui.courieragreement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.ui.NetworkViewModel

class CourierAgreementViewModel(compositeDisposable: CompositeDisposable) :
    NetworkViewModel(compositeDisposable) {

    private val _navigationState = MutableLiveData<CourierAgreementNavigationState>()
    val navigationState: LiveData<CourierAgreementNavigationState>
        get() = _navigationState

    fun onCancelClick() {
        _navigationState.value = CourierAgreementNavigationState.Cancel

    }

    fun onCompleteClick() {
        _navigationState.value = CourierAgreementNavigationState.Complete
    }

}
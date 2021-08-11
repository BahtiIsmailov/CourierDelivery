package ru.wb.perevozka.ui.userdata.couriers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.userdata.couriers.domain.CouriersCompleteRegistrationInteractor

class CouriersCompleteRegistrationViewModel(
    parameters: CouriersCompleteRegistrationParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CouriersCompleteRegistrationResourceProvider,
    private val interactor: CouriersCompleteRegistrationInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<String>()
    val infoState: LiveData<String>
        get() = _infoState

    private val _navigateToDialog = MutableLiveData<CouriersCompleteRegistrationNavAction>()
    val navigateToUpdateDialogInfo: LiveData<CouriersCompleteRegistrationNavAction>
        get() = _navigateToDialog

    private val _progressState = SingleLiveEvent<CouriersCompleteRegistrationProgressState>()
    val progressState: LiveData<CouriersCompleteRegistrationProgressState>
        get() = _progressState

    fun onUpdateStatusClick() {
        _progressState.value = CouriersCompleteRegistrationProgressState.Progress
        addSubscription(
            interactor.isRegisteredStatus()
                .map {
                    when (it) {
                        true -> CouriersCompleteRegistrationNavAction.NavigateToApplication
                        false -> CouriersCompleteRegistrationNavAction.NavigateToCouriersDialog
                    }
                }.subscribe(
                    {
                        _progressState.value = CouriersCompleteRegistrationProgressState.Complete
                        _navigateToDialog.value = it
                    },
                    {
                        _progressState.value = CouriersCompleteRegistrationProgressState.Complete
                        _navigateToDialog.value =
                            CouriersCompleteRegistrationNavAction.NavigateToCouriersDialog
                    })
        )

    }

}
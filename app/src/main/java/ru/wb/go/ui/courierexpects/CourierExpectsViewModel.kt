package ru.wb.go.ui.courierexpects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierexpects.domain.CourierExpectsInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo

class CouriersCompleteRegistrationViewModel(
    parameters: CourierExpectsParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CourierExpectsResourceProvider,
    private val interactor: CourierExpectsInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _navigateToMessageState = SingleLiveEvent<NavigateToDialogInfo>()
    val navigateToMessageState: LiveData<NavigateToDialogInfo>
        get() = _navigateToMessageState

    private val _infoState = MutableLiveData<String>()
    val infoState: LiveData<String>
        get() = _infoState

    private val _navAction = MutableLiveData<CourierExpectsNavAction>()
    val navigationState: LiveData<CourierExpectsNavAction>
        get() = _navAction

    private val _progressState = SingleLiveEvent<CourierExpectsProgressState>()
    val progressState: LiveData<CourierExpectsProgressState>
        get() = _progressState

    fun onUpdateStatusClick() {
        _progressState.value = CourierExpectsProgressState.Progress
        addSubscription(
            interactor.isRegisteredStatus().subscribe(
                { isRegisteredStatusComplete(it) },
                { isRegisteredStatusError(it) })
        )

    }

    private fun isRegisteredStatusComplete(it: Boolean?) {
        _progressState.value = CourierExpectsProgressState.Complete
        when (it) {
            true -> _navAction.value = CourierExpectsNavAction.NavigateToCouriers
            false -> _navigateToMessageState.value = NavigateToDialogInfo(
                DialogInfoStyle.INFO.ordinal,
                resourceProvider.notConfirmDataTitle(),
                resourceProvider.notConfirmDataMessage(),
                resourceProvider.notConfirmDataPositive()
            )
        }
    }

    private fun isRegisteredStatusError(throwable: Throwable) {
        _progressState.value = CourierExpectsProgressState.Complete
        when (throwable) {
            is NoInternetException -> _navigateToMessageState.value = NavigateToDialogInfo(
                DialogInfoStyle.WARNING.ordinal,
                resourceProvider.getGenericInternetTitleError(),
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> {
                _navigateToMessageState.value = NavigateToDialogInfo(
                    DialogInfoStyle.WARNING.ordinal,
                    resourceProvider.getGenericServiceTitleError(),
                    throwable.error.message,
                    resourceProvider.getGenericServiceButtonError()
                )
            }
            else -> _navigateToMessageState.value = NavigateToDialogInfo(
                DialogInfoStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                throwable.toString(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
    }
}
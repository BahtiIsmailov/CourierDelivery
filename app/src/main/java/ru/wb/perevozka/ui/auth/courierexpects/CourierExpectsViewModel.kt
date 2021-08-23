package ru.wb.perevozka.ui.auth.courierexpects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.auth.courierexpects.domain.CourierExpectsInteractor
import ru.wb.perevozka.ui.dialogs.DialogStyle

class CouriersCompleteRegistrationViewModel(
    parameters: CourierExpectsParameters,
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: CourierExpectsResourceProvider,
    private val interactor: CourierExpectsInteractor,
) : NetworkViewModel(compositeDisposable) {

    private val _navigateToMessageState = SingleLiveEvent<Message>()
    val navigateToMessageState: LiveData<Message>
        get() = _navigateToMessageState

    private val _infoState = MutableLiveData<String>()
    val infoState: LiveData<String>
        get() = _infoState

    private val _navAction = MutableLiveData<CourierExpectsNavAction>()
    val navAction: LiveData<CourierExpectsNavAction>
        get() = _navAction

    private val _progressState = SingleLiveEvent<CourierExpectsProgressState>()
    val progressState: LiveData<CourierExpectsProgressState>
        get() = _progressState

    fun onUpdateStatusClick() {
        _progressState.value = CourierExpectsProgressState.Progress
        addSubscription(
            interactor.isRegisteredStatus()
                .map {
                    when (it) {
                        true -> CourierExpectsNavAction.NavigateToApplication
                        false -> CourierExpectsNavAction.NavigateToCouriersDialog(
                            DialogStyle.INFO.ordinal,
                            resourceProvider.notConfirmDataTitle(),
                            resourceProvider.notConfirmDataMessage(),
                            resourceProvider.notConfirmDataPositive()
                        )
                    }
                }.subscribe(
                    {
                        _progressState.value = CourierExpectsProgressState.Complete
                        _navAction.value = it
                    },
                    { isRegisteredStatusError(it) })
        )

    }

    private fun isRegisteredStatusError(throwable: Throwable) {
        _progressState.value = CourierExpectsProgressState.Complete
        when (throwable) {
            is NoInternetException -> _navigateToMessageState.value = Message(
                DialogStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> {
                _navigateToMessageState.value = Message(
                    DialogStyle.WARNING.ordinal,
                    throwable.error.message,
                    resourceProvider.getGenericServiceMessageError(),
                    resourceProvider.getGenericServiceButtonError()
                )
            }
            else -> _navigateToMessageState.value = Message(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }
    }
}

data class Message(val style: Int, val title: String, val message: String, val button: String)
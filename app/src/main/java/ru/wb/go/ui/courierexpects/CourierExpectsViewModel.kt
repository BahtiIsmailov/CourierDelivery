package ru.wb.go.ui.courierexpects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierexpects.domain.CourierExpectsInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.analytics.YandexMetricManager

class CouriersCompleteRegistrationViewModel(
    private val parameters: CourierExpectsParameters,

    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,

    private val resourceProvider: CourierExpectsResourceProvider,
    private val interactor: CourierExpectsInteractor,
    private val appRemoteRepository: AppRemoteRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,

    ) : NetworkViewModel(compositeDisposable, metric) {

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

    init {
        onTechEventLog("init")
    }

    fun onUpdateStatusClick() {
        onTechEventLog("onUpdateStatusClick")
        _progressState.value = CourierExpectsProgressState.Progress
        addSubscription(
            interactor.isRegisteredStatus().subscribe(
                { isRegisteredStatusComplete(it) },
                { isRegisteredStatusError(it) })
        )

    }

    private fun isRegisteredStatusComplete(registerStatus: String?) {
        onTechEventLog("isRegisteredStatusComplete")
        when (registerStatus) {
            NEED_SEND_COURIER_DOCUMENTS -> {
                _navAction.value =
                    CourierExpectsNavAction.NavigateToRegistrationCouriers(
                        parameters.phone,
                        CourierDocumentsEntity()
                    )
            }
            NEED_CORRECT_COURIER_DOCUMENTS -> {
                addSubscription(
                    appRemoteRepository.getCourierDocuments()
                        .compose(rxSchedulerFactory.applySingleSchedulers())
                        .subscribe({
                            _navAction.value =
                                CourierExpectsNavAction.NavigateToRegistrationCouriers(
                                    parameters.phone, it
                                )

                        }, { isRegisteredStatusError(it) })
                )

            }
            NEED_APPROVE_COURIER_DOCUMENTS -> {
                _navigateToMessageState.value = NavigateToDialogInfo(
                    DialogInfoStyle.INFO.ordinal,
                    resourceProvider.notConfirmDataTitle(),
                    resourceProvider.notConfirmDataMessage(),
                    resourceProvider.notConfirmDataPositive()
                )
                _progressState.value = CourierExpectsProgressState.Complete
            }
            else -> {
                //TODO не отображается ФИО при этом переходе
                _navAction.value =
                    CourierExpectsNavAction.NavigateToCouriers
            }
        }
    }

    private fun isRegisteredStatusError(throwable: Throwable) {
        onTechErrorLog("isRegisteredStatusError", throwable)
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

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CouriersCompleteRegistration"
    }

}
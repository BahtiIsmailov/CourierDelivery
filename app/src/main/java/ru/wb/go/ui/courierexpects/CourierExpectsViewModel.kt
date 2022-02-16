package ru.wb.go.ui.courierexpects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.exceptions.CustomException
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierexpects.domain.CourierExpectsInteractor
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class CouriersCompleteRegistrationViewModel(
    private val parameters: CourierExpectsParameters,

    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,

    private val resourceProvider: CourierExpectsResourceProvider,
    private val interactor: CourierExpectsInteractor,
    private val appRemoteRepository: AppRemoteRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val errorDialogManager: ErrorDialogManager,
    private val tokenManager: TokenManager,
) : NetworkViewModel(compositeDisposable, metric) {

    private val _showDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val showDialogInfo: LiveData<ErrorDialogData>
        get() = _showDialogInfo

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
                {
                    _progressState.value = CourierExpectsProgressState.Complete
                    isRegisteredStatusComplete(it)
                },
                {
                    _progressState.value = CourierExpectsProgressState.Complete
                    errorDialogManager.showErrorDialog(it, _showDialogInfo)
                })
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

                        }, { errorDialogManager.showErrorDialog(it, _showDialogInfo) })
                )

            }
            NEED_APPROVE_COURIER_DOCUMENTS -> {
                val th = CustomException(resourceProvider.notConfirmDataMessage())
                errorDialogManager.showErrorDialog(th, _showDialogInfo)
            }
            else -> {
                if (tokenManager.isUserCourier()) {
                    //TODO не отображается ФИО при этом переходе
                    _navAction.value = CourierExpectsNavAction.NavigateToCouriers
                } else {
                    // TODO: Ждем появления этого кейса
                    val ce = CustomException("Неизвестная ошибка")
                    onTechErrorLog("CheckRegistrationStatus", ce)
                    errorDialogManager.showErrorDialog(ce, _showDialogInfo)
                }

            }
        }
    }


    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CouriersCompleteRegistration"
    }

}
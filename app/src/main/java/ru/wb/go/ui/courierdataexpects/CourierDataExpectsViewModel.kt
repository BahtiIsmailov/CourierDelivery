package ru.wb.go.ui.courierdataexpects

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
import ru.wb.go.ui.app.domain.AppNavRepository
import ru.wb.go.ui.app.domain.AppNavRepositoryImpl.Companion.INVALID_TOKEN
import ru.wb.go.ui.courierdataexpects.domain.CourierDataExpectsInteractor
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class CouriersCompleteRegistrationViewModel(
    private val parametersData: CourierDataExpectsParameters,

    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,

    private val resourceProviderData: CourierDataExpectsResourceProvider,
    private val interactorData: CourierDataExpectsInteractor,
    private val appRemoteRepository: AppRemoteRepository,
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val errorDialogManager: ErrorDialogManager,
    private val tokenManager: TokenManager,
    private val appNavRepository: AppNavRepository
) : NetworkViewModel(compositeDisposable, metric) {

    private val _showDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val showDialogInfo: LiveData<ErrorDialogData>
        get() = _showDialogInfo

    private val _navAction = MutableLiveData<CourierDataExpectsNavAction>()
    val navigationState: LiveData<CourierDataExpectsNavAction>
        get() = _navAction

    private val _progressState = SingleLiveEvent<CourierDataExpectsProgressState>()
    val progressStateData: LiveData<CourierDataExpectsProgressState>
        get() = _progressState

    init {
        onTechEventLog("init")
    }

    fun onUpdateStatusClick() {
        onTechEventLog("onUpdateStatusClick")
        _progressState.value = CourierDataExpectsProgressState.ProgressData
        addSubscription(
            interactorData.isRegisteredStatus()
                .subscribe(
                    { isRegisteredStatusComplete(it) },
                    {
                        errorDialogManager.showErrorDialog(it, _showDialogInfo)
                        _progressState.value = CourierDataExpectsProgressState.Complete
                    })
        )
    }

    private fun isRegisteredStatusComplete(registerStatus: String?) {
        onTechEventLog("isRegisteredStatusComplete")
        when (registerStatus) {
            INVALID_TOKEN -> appNavRepository.navigate(INVALID_TOKEN)
            NEED_SEND_COURIER_DOCUMENTS -> toDataType(CourierDocumentsEntity())
            NEED_CORRECT_COURIER_DOCUMENTS -> checkCorrectCourierDocuments()
            NEED_APPROVE_COURIER_DOCUMENTS -> checkApproveCourierDocuments()
            else -> {
                if (tokenManager.isUserCourier()) {
                    //TODO не отображается ФИО при этом переходе
                    _navAction.value = CourierDataExpectsNavAction.NavigateToCouriers
                } else {
                    val ce = CustomException("Unknown error")
                    onTechErrorLog("CheckRegistrationStatus", ce)
                    errorDialogManager.showErrorDialog(ce, _showDialogInfo)
                }

            }
        }
    }

    private fun checkApproveCourierDocuments() {
        val th = CustomException(resourceProviderData.notConfirmDataMessage())
        errorDialogManager.showErrorDialog(th, _showDialogInfo)
        _progressState.value = CourierDataExpectsProgressState.Complete
    }

    private fun checkCorrectCourierDocuments() {
        addSubscription(
            appRemoteRepository.getCourierDocuments()
                .compose(rxSchedulerFactory.applySingleSchedulers())
                .subscribe(
                    { toDataType(it) },
                    {
                        errorDialogManager.showErrorDialog(it, _showDialogInfo)
                        _progressState.value = CourierDataExpectsProgressState.Complete
                    })
        )
    }

    private fun toDataType(it: CourierDocumentsEntity) {
        _navAction.value =
            CourierDataExpectsNavAction.NavigateToDataType(
                parametersData.phone, it
            )
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CouriersCompleteRegistration"
    }

}
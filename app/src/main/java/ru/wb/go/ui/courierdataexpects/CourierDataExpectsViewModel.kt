package ru.wb.go.ui.courierdataexpects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import ru.wb.go.app.INTERNAL_SERVER_ERROR_COURIER_DOCUMENTS
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
import ru.wb.go.ui.courierdataexpects.CourierDataExpectsFragment.Companion.DIALOG_EXPECTS_ERROR_RESULT_TAG
import ru.wb.go.ui.courierdataexpects.domain.CourierDataExpectsInteractor
import ru.wb.go.utils.analytics.YandexMetricManager
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager
import java.lang.Exception

class CouriersCompleteRegistrationViewModel(
    private val parametersData: CourierDataExpectsParameters,

    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,

    private val resourceProviderData: CourierDataExpectsResourceProvider,
    private val interactorData: CourierDataExpectsInteractor,
    private val appRemoteRepository: AppRemoteRepository,
    private val errorDialogManager: ErrorDialogManager,
    private val tokenManager: TokenManager,
    private val appNavRepository: AppNavRepository
) : NetworkViewModel(compositeDisposable, metric) {

    private val _showDialogInfo = SingleLiveEvent<ErrorDialogData>()
    val showDialogInfo: LiveData<ErrorDialogData>
        get() = _showDialogInfo

    private val _showErrorDialog = SingleLiveEvent<ErrorDialogData>()
    val navigateToErrorDialog: LiveData<ErrorDialogData>
        get() = _showErrorDialog

    private val _navAction = MutableLiveData<CourierDataExpectsNavAction>()
    val navigationState: LiveData<CourierDataExpectsNavAction>
        get() = _navAction

    private val _progressState = SingleLiveEvent<CourierDataExpectsProgressState>()
    val progressStateData: LiveData<CourierDataExpectsProgressState>
        get() = _progressState

    init {
        onTechEventLog("init")
        _progressState.value = CourierDataExpectsProgressState.ProgressData
        viewModelScope.launch {
            try{
                interactorData.saveRepeatCourierDocuments()
                _progressState.postValue(CourierDataExpectsProgressState.Complete)
            }catch (e:Exception){
                _progressState.postValue(CourierDataExpectsProgressState.Complete)
            }
        }
    }

    fun onUpdateStatusClick() {
        onTechEventLog("onUpdateStatusClick")
        _progressState.postValue(CourierDataExpectsProgressState.ProgressData)
        viewModelScope.launch {
            try{
                interactorData.saveRepeatCourierDocuments()
                val  result = interactorData.isRegisteredStatus()
                isRegisteredStatusComplete(result)
            }catch (e:Exception){
                isRegisteredStatusError(e)
            }
        }
    }

    private fun isRegisteredStatusComplete(registerStatus: String?) {
        onTechEventLog("isRegisteredStatusComplete")
        when (registerStatus) {
            INTERNAL_SERVER_ERROR_COURIER_DOCUMENTS -> _progressState.value =
                CourierDataExpectsProgressState.Complete
            INVALID_TOKEN -> appNavRepository.navigate(INVALID_TOKEN)
            NEED_SEND_COURIER_DOCUMENTS -> toDataType(CourierDocumentsEntity())
            NEED_CORRECT_COURIER_DOCUMENTS -> checkCorrectCourierDocuments()
            NEED_APPROVE_COURIER_DOCUMENTS -> checkApproveCourierDocuments()
            else -> {
                if (tokenManager.isUserCourier()) {
                    //TODO не отображается ФИО при этом переходе
                    _navAction.postValue(CourierDataExpectsNavAction.NavigateToCouriers)
                } else {
                    val ce = CustomException("Unknown error")
                    onTechErrorLog("CheckRegistrationStatus", ce)
                    errorDialogManager.showErrorDialog(ce, _showDialogInfo)
                }

            }
        }
    }

    private fun isRegisteredStatusError(it: Throwable) {
        errorDialogManager.showErrorDialog(it, _showDialogInfo)
        _progressState.postValue(CourierDataExpectsProgressState.Complete)
    }

    private fun checkApproveCourierDocuments() {
        val th = CustomException(resourceProviderData.notConfirmDataMessage())
        errorDialogManager.showErrorDialog(th, _showDialogInfo)
        _progressState.postValue(CourierDataExpectsProgressState.Complete)
    }

    private fun checkCorrectCourierDocuments() {
        viewModelScope.launch {
            try {
                val response = appRemoteRepository.getCourierDocuments()
                checkCorrectCourierDocumentsComplete(response)
            }catch (e:Exception){
                errorDialogManager.showErrorDialog(e, _showDialogInfo)
                _progressState.postValue(CourierDataExpectsProgressState.Complete)
            }
        }
//        addSubscription(
//            appRemoteRepository.getCourierDocuments()
//                .compose(rxSchedulerFactory.applySingleSchedulers())
//                .subscribe(
//                    { checkCorrectCourierDocumentsComplete(it) },
//                    {
//                        errorDialogManager.showErrorDialog(it, _showDialogInfo)
//                        _progressState.value = CourierDataExpectsProgressState.Complete
//                    })
//        )
    }

    private lateinit var courierDocumentsEntityDialog: CourierDocumentsEntity

    private fun checkCorrectCourierDocumentsComplete(it: CourierDocumentsEntity) {
        courierDocumentsEntityDialog = it
        val errorAnnotate = CustomException(it.errorAnnotate!!)
        errorDialogManager.showErrorDialog(
            errorAnnotate,
            _showErrorDialog,
            DIALOG_EXPECTS_ERROR_RESULT_TAG
        )
    }

    fun onErrorDialogConfirmClick() {
        toDataType(courierDocumentsEntityDialog)
    }

    private fun toDataType(it: CourierDocumentsEntity) {
        _navAction.value = CourierDataExpectsNavAction.NavigateToDataType(parametersData.phone, it)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "CouriersCompleteRegistration"
    }

}
package ru.wb.go.ui.courierdata


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.exceptions.InternalServerException
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.courierdata.domain.CourierDataInteractor
import ru.wb.go.utils.managers.ErrorDialogData
import ru.wb.go.utils.managers.ErrorDialogManager

class UserFormViewModel(
    private val parameters: CourierDataParameters,
    private val interactor: CourierDataInteractor,
    private val errorDialogManager: ErrorDialogManager,
) : NetworkViewModel() {

    private val _navigateToMessageInfo = SingleLiveEvent<ErrorDialogData>()
    val navigateToMessageInfo: LiveData<ErrorDialogData>
        get() = _navigateToMessageInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    private val _navigationEvent =
        SingleLiveEvent<CourierDataNavAction>()
    val navigationEvent: LiveData<CourierDataNavAction>
        get() = _navigationEvent

    private val _formUIState = MutableLiveData<CourierDataUIState>()
    val formUIState: LiveData<CourierDataUIState>
        get() = _formUIState

    private val _loaderState = MutableLiveData<CourierDataUILoaderState>()
    val loaderState: LiveData<CourierDataUILoaderState>
        get() = _loaderState



    init {
         observeNetworkState()
    }

    private fun checkTextSurnameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)


    private fun checkInputRequisite(field: CourierDataUIAction.TextChange): CourierDataUIState {
        val pattern = when (field.type) {
            CourierDataQueryType.MIDDLE_NAME -> """^\$|[а-яА-ЯёЁ -.`']+$"""
            CourierDataQueryType.INN -> """^\d{12}$"""
            CourierDataQueryType.PASSPORT_SERIES -> """^\d{4}$"""
            CourierDataQueryType.PASSPORT_NUMBER -> """^\d{6}$"""
            CourierDataQueryType.PASSPORT_DATE -> """^.+$"""
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> """^\d{6}$"""
            CourierDataQueryType.PASSPORT_ISSUED_BY -> """^.+$"""
            else -> """^[а-яА-ЯёЁ -.`']+$"""
        }
        val regex = Regex(pattern)

        return if (!regex.matches(field.text)) {
            CourierDataUIState.Error(field.text, field.type)
        } else {
            CourierDataUIState.Complete(field.text, field.type)
        }
    }

    private fun checkTextNameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextMiddleNameWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextInnWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportSeriesWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportNumberWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportDateWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportIssuedByWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    private fun checkTextPassportDepartmentCodeWrapper(focusChange: CourierDataUIAction.TextChange): CourierDataUIState =
        checkInputRequisite(focusChange)

    fun onFormChanges(changeObservables: ArrayList<Flow<CourierDataUIAction>>) {
        changeObservables.merge()
            .map {
                mapAction(it)
            }
            .onEach {
                _formUIState.value = it
            }
            .catch {
                logException(it,"onFormChanges")

            }
            .launchIn(viewModelScope)
    }

    private fun mapAction(action: CourierDataUIAction) = when (action) {
        is CourierDataUIAction.TextChange -> checkFieldText(action)
        is CourierDataUIAction.CompleteClick -> checkFieldAll(action)
    }

    private fun checkFieldAll(action: CourierDataUIAction.CompleteClick): CourierDataUIState {
        val iterator = action.userData.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()
            val field = CourierDataUIAction.TextChange(item.text, item.type)
            if (isNotCheck(checkInputRequisite(field))) {
                return CourierDataUIState.ErrorFocus("", item.type)
            }

            iterator.remove()
        }
        return CourierDataUIState.Next

    }

    private fun checkFieldText(action: CourierDataUIAction.TextChange) =
        when (action.type) {
            CourierDataQueryType.SURNAME -> checkTextSurnameWrapper(action)
            CourierDataQueryType.NAME -> checkTextNameWrapper(action)
            CourierDataQueryType.MIDDLE_NAME -> checkTextMiddleNameWrapper(action)
            CourierDataQueryType.INN -> checkTextInnWrapper(action)
            CourierDataQueryType.PASSPORT_SERIES -> checkTextPassportSeriesWrapper(action)
            CourierDataQueryType.PASSPORT_NUMBER -> checkTextPassportNumberWrapper(action)
            CourierDataQueryType.PASSPORT_DATE -> checkTextPassportDateWrapper(action)
            CourierDataQueryType.PASSPORT_DEPARTMENT_CODE -> checkTextPassportDepartmentCodeWrapper(
                action
            )
            CourierDataQueryType.PASSPORT_ISSUED_BY -> checkTextPassportIssuedByWrapper(action)
        }

    private fun isNotCheck(state: CourierDataUIState) = state !is CourierDataUIState.Complete


    fun onNextClick(courierDocumentsEntity: CourierDocumentsEntity) {
        _loaderState.value = CourierDataUILoaderState.Progress
        courierDocumentsEntity.courierType = parameters.docs.courierType
        viewModelScope.launch {
            try {
                interactor.saveCourierDocuments(courierDocumentsEntity)
                couriersFormComplete()
            }catch (e:Exception){
                logException(e,"onNextClick")
                couriersFormError(e)
            }
        }
    }


    fun onCheckedClick(isAgreement: Boolean) {
        _loaderState.value = if (isAgreement) {
            CourierDataUILoaderState.Enable
        } else {
            CourierDataUILoaderState.Disable
        }
    }

    private fun couriersFormComplete() {
        //onTechEventLog("couriersFormComplete", "NavigateToCouriersCompleteRegistration")
        _loaderState.value = CourierDataUILoaderState.Disable
        _navigationEvent.value =
            CourierDataNavAction.NavigateToCouriersCompleteRegistration(parameters.phone)
    }

    private fun couriersFormError(it: Throwable) {
        //onTechEventLog("couriersFormError", it)
        _loaderState.value = CourierDataUILoaderState.Enable
        if (it is InternalServerException) couriersFormComplete()
        else errorDialogManager.showErrorDialog(it, _navigateToMessageInfo)
    }

    private fun observeNetworkState() {
        interactor.observeNetworkConnected()
            .onEach {
                _toolbarNetworkState.value = it
            }
            .launchIn(viewModelScope)
    }

    fun onShowAgreementClick() {
        //onTechEventLog("onShowAgreementClick")
        _navigationEvent.value = CourierDataNavAction.NavigateToAgreement
    }

    fun getParams(): CourierDataParameters {
        return parameters
    }

     fun decodeToUTF8(data: String): String {
        val resultWord = data.map {
            println(it.code)
            if (it.code == 235) {
                it.toString().encodeToByteArray()
                val buffer = it.toString().toByteArray(charset("ASCII"))
                val letter = String(buffer, charset("ASCII"))
                letter

            } else {
                it
            }
        }.joinToString(",").replace(",", "").replace("?", "ё")
        print("$resultWord ")
        return resultWord
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "UserForm"
    }

 }
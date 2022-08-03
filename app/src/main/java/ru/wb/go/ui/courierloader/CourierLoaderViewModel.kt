package ru.wb.go.ui.courierloader

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courierlocal.LocalComplexOrderEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.courierdata.CourierDataParameters
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.SettingsManager
import ru.wb.go.utils.prefs.SharedWorker
import java.net.UnknownHostException

class CourierLoaderViewModel(
    private val tokenManager: TokenManager,
    private val locRepo: CourierLocalRepository,
    private val remoteRepo: AppRemoteRepository,
    private val deviceManager: DeviceManager,
    private val resourceProvider: CourierLoaderResourceProvider,
    private val settingsManager: SettingsManager,
    private val userManager: UserManager,
    private val sharedWorker: SharedWorker
) : NetworkViewModel() {

    private val _drawerHeader = MutableLiveData<UserInfoEntity>()
    val drawerHeader: LiveData<UserInfoEntity>
        get() = _drawerHeader

    private val _navigationDrawerState = MutableLiveData<CourierLoaderNavigationState>()
    val navigationDrawerState: LiveData<CourierLoaderNavigationState>
        get() = _navigationDrawerState

    private val _state = MutableLiveData<CourierLoaderUIState>()
    val state: LiveData<CourierLoaderUIState>
        get() = _state


    init {
        initDrawer()
        initVersion()
        checkRootState()
        sharedWorker.saveMediate(AppPreffsKeys.FRAGMENT_MANAGER,"fromSms")
    }

    private fun initDrawer() {
        _drawerHeader.value = UserInfoEntity(tokenManager.userName(), tokenManager.userCompany(),tokenManager.wbUserID())
    }

    fun initVersion() {
        _state.value = CourierLoaderUIState.Progress
        viewModelScope.launch {
            try {
                val response = remoteRepo.appVersion()
                appVersionUpdateComplete(response)
            } catch (e: Exception) {
                logException(e,"initVersion")
                appVersionUpdateError()
            }
        }
    }

    private fun appVersionUpdateComplete(version: String) {
        checkUserState(version)
    }

    private fun appVersionUpdateError() {
        checkUserState("0.0.0")
    }

    private fun goToUpdate(version: String): Boolean {
        val res = !deviceManager.isAppVersionActual(version)
        if (res)
            toAppUpdate()
        return res
    }

    private fun checkRootState() {
        if (resourceProvider.isRooted()) {
        }
    }

    private fun checkUserState(version: String) {
        viewModelScope.launch {

            val phone = tokenManager.userPhone()

            checkNewInstallation()

            val order = locRepo.getOrder()

            if (order == null && goToUpdate(version)) {
                return@launch
            }

            if (userManager.courierDocumentsEntity() != null) {
                toCourierDataExpects(phone)
                return@launch
            }

            when {
                tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) ->
                    if (!goToUpdate(version)) toNewRegistration(phone)
                tokenManager.resources().contains(NEED_CORRECT_COURIER_DOCUMENTS) ->
                    if (!goToUpdate(version)) toRegistration(phone)
                tokenManager.resources().contains(NEED_APPROVE_COURIER_DOCUMENTS) ->
                    if (!goToUpdate(version)) toCourierDataExpects(phone)
                tokenManager.isUserCourier() -> toApp(order)
                else -> {
                    assert(tokenManager.isDemo())
                    _navigationDrawerState.value = toCourierWarehouse()
                }
            }
        }
    }

    private fun toApp(order: LocalOrderEntity?) {
        viewModelScope.launch {
            try {
                val orderFromRemote = remoteRepo.tasksMy()
                val res = solveJobInitialState(orderFromRemote, order)
                _navigationDrawerState.value = res
            } catch (e: Exception) {
                logException(e,"toApp")
                onRxError(e)
            }
        }

    }

     private fun solveJobInitialState(
        remoteOrder: LocalComplexOrderEntity,
        order: LocalOrderEntity?
    ): CourierLoaderNavigationState {

        val remoteTaskId = remoteOrder.order.orderId
        return when {
            (order == null || remoteTaskId != order.orderId) && (remoteTaskId != -2) -> {
                syncFromServer(remoteOrder)
                getNavigationState(remoteOrder.order.status)
            }
            else -> {
                val localStatus = order!!.status
                getNavigationState(localStatus)
            }
        }
}


private fun syncFromServer(
    remoteOrder: LocalComplexOrderEntity,
) {
    clearCurrentLocalData()
    assert(remoteOrder.order.orderId != -2)
    if (remoteOrder.order.orderId < 0) {
        return
    }
    viewModelScope.launch {
        val it = remoteRepo.taskBoxes(remoteOrder.order.orderId.toString())
        locRepo.saveRemoteOrder(remoteOrder, it)
    }
}

private fun getNavigationState(status: String) =
    when (status) {
        TaskStatus.TIMER.status -> toTimer()
        TaskStatus.STARTED.status -> toLoadingScanner()
        TaskStatus.INTRANSIT.status -> toIntransit()
        else -> toCourierWarehouse()
    }

private fun onRxError(throwable: Throwable) {
    when (throwable) {
        is NullPointerException -> {
            clearCurrentLocalData()
            _state.value = CourierLoaderUIState.Complete
            _navigationDrawerState.value = toCourierWarehouse()
        }
        is BadRequestException -> {
            _state.value = CourierLoaderUIState.Error(throwable.message.toString())
        }
        is NoInternetException, is UnknownHostException, is IllegalStateException -> {
            _state.value = CourierLoaderUIState.Error("Возможно отсутствует интернет или низкая скорость соединения")
        }
        else -> {
            _state.value = CourierLoaderUIState.Error(throwable.toString())
        }
    }
}

private fun clearCurrentLocalData() {
    viewModelScope.launch {
        locRepo.clearOrder()
    }
}

private fun toNewRegistration(phone: String) {
    _navigationDrawerState.value =
        CourierLoaderNavigationState.NavigateToCourierDataType(
            CourierDataParameters(phone = phone, docs = CourierDocumentsEntity())
        )
}

private fun toRegistration(phone: String) {
    viewModelScope.launch {
        try {
            val response = remoteRepo.getCourierDocuments()
            val courierDataParameters = CourierDataParameters(phone = phone, docs = response)
            val responseState =
                CourierLoaderNavigationState.NavigateToCourierDataType(courierDataParameters)
            _navigationDrawerState.value = responseState

        } catch (e: Exception) {
            logException(e,"toRegistration")
            onRxError(e)
        }
    }
}

private fun toCourierDataExpects(phone: String) {
    _navigationDrawerState.value =
        CourierLoaderNavigationState.NavigateToCouriersCompleteRegistration(phone)
}

private fun toCourierWarehouse() = CourierLoaderNavigationState.NavigateToCourierWarehouse

private fun toTimer() = CourierLoaderNavigationState.NavigateToTimer

private fun toLoadingScanner() = CourierLoaderNavigationState.NavigateToScanner

private fun toIntransit() = CourierLoaderNavigationState.NavigateToIntransit

private fun toAppUpdate() {
    _navigationDrawerState.value = CourierLoaderNavigationState.NavigateToCourierWarehouse
    //TODO(тут было на обновление состояние APPUPDATE)
}

private fun checkNewInstallation() {
    if (settingsManager.checkNewInstall(deviceManager.appVersion)) {
        //onTechEventLog("newInstallDetected", deviceManager.appVersion)
    }
}

override fun getScreenTag(): String {
    return SCREEN_TAG
}

companion object {
    const val SCREEN_TAG = "CourierLoader"
}

}


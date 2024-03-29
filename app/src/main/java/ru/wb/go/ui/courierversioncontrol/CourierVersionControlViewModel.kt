package ru.wb.go.ui.courierversioncontrol

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.managers.DeviceManager

class CourierVersionControlViewModel(
        private val resourceProvider: CourierVersionControlResourceProvider,
        private val deviceManager: DeviceManager,
) : NetworkViewModel() {

    private val _versionTitleState = MutableLiveData<String>()
    val versionTitleState: LiveData<String>
        get() = _versionTitleState

    private val _navigateToBack = MutableLiveData<NavigateToWarehouse>()
    val navigateToBack: LiveData<NavigateToWarehouse>
        get() = _navigateToBack

    private val _updateFromGooglePlay = MutableLiveData<UpdateFromGooglePlay>()
    val updateFromGooglePlay: LiveData<UpdateFromGooglePlay>
        get() = _updateFromGooglePlay

    init {
        _versionTitleState.value = resourceProvider.getAvailableVersion(deviceManager.appAdminVersion)
    }

    fun onUpdateClick() {
        //onTechEventLog("onUpdateClick")
        val packageName: String = deviceManager.appPackageName
        _updateFromGooglePlay.value = UpdateFromGooglePlay(
                resourceProvider.getUriPlayMarket(packageName),
                resourceProvider.getUriGoogle(packageName)
        )
    }

    object NavigateToWarehouse
    data class UpdateFromGooglePlay(val uriPlayMarket: String, val packageName: String)

    companion object {
        const val SCREEN_TAG = "CourierVersionControl"
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

}


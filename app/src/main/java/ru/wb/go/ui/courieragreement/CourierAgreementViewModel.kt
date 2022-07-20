package ru.wb.go.ui.courieragreement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.wb.go.app.AppPreffsKeys
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.utils.managers.SettingsManager

class CourierAgreementViewModel(
    private val settingsManager: SettingsManager
) :
    NetworkViewModel() {

    private val _navigationState = MutableLiveData<CourierAgreementNavigationState>()
    val navigationState: LiveData<CourierAgreementNavigationState>
        get() = _navigationState

    fun onCompleteClick() {
        //onTechEventLog("onCompleteClick")
        _navigationState.value = CourierAgreementNavigationState.Complete
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    fun getDarkThemeSetting(): Boolean {
        return settingsManager.getSetting(
            AppPreffsKeys.SETTING_THEME,
            false
        )
    }

    companion object {
        const val SCREEN_TAG = "CourierAgreement"
    }

}
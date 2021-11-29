package ru.wb.go.ui.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.wb.go.ui.config.data.KeyValueDao
import ru.wb.go.utils.managers.ConfigManager
import ru.wb.go.utils.managers.DeviceManager

class ConfigViewModel(
    private val configManager: ConfigManager,
    private val deviceManager: DeviceManager
) : ViewModel() {

    private var authServerValueSelected: KeyValueDao? = null
    private var appServerValueSelected: KeyValueDao? = null
    private var autoSubstitutionChecked = false
    private var autoAuthorizationChecked = false

    private val _authServerValues = MutableLiveData<List<KeyValueDao>>()
    val authServerValues: LiveData<List<KeyValueDao>>
        get() = _authServerValues

    private val _authServerSelect = MutableLiveData<KeyValueDao>()
    val authServerSelect: LiveData<KeyValueDao>
        get() = _authServerSelect

    private val _appServerValues = MutableLiveData<List<KeyValueDao>>()
    val appServerValues: LiveData<List<KeyValueDao>>
        get() = _appServerValues

    private val _appServerSelect = MutableLiveData<KeyValueDao>()
    val appServerSelect: LiveData<KeyValueDao>
        get() = _appServerSelect


    init {
        fetchCount()
    }

    private fun fetchCount() {
        _authServerValues.value = configManager.authServersUrl
        _authServerSelect.value = configManager.readDaoAuthServerUrl()
        _appServerValues.value = configManager.appServersUrl
        _appServerSelect.value = configManager.readDaoAppServerUrl()
    }

    fun onAuthServerSelected(keyValue: KeyValueDao) {
        authServerValueSelected = keyValue
    }

    fun onAppServerSelected(keyValue: KeyValueDao) {
        appServerValueSelected = keyValue
    }

    fun onAutoSubstitutionChecked(isChecked: Boolean) {
        autoSubstitutionChecked = isChecked
    }

    fun onAutoAuthorizationChecked(isChecked: Boolean) {
        autoAuthorizationChecked = isChecked
    }

    fun onRestartClicked() {
        saveConfigDataAndRestartClick()
    }

    private fun saveConfigDataAndRestartClick() {
        authServerValueSelected?.let { configManager.saveAuthServerUrl(it) }
        appServerValueSelected?.let { configManager.saveAppServerUrl(it) }
        deviceManager.doRestart()
    }

}
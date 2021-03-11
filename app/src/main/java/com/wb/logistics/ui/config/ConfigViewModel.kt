package com.wb.logistics.ui.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wb.logistics.ui.config.dao.KeyValueDAO
import com.wb.logistics.utils.managers.ConfigManager
import com.wb.logistics.utils.managers.DeviceManager

class ConfigViewModel(
    private val configManager: ConfigManager,
    private val deviceManager: DeviceManager
) : ViewModel() {

    private var authServerValueSelected: KeyValueDAO? = null
    private var autoSubstitutionChecked = false
    private var autoAuthorizationChecked = false

    private val _authServerValues = MutableLiveData<List<KeyValueDAO>>()
    val authServerValues: LiveData<List<KeyValueDAO>>
        get() = _authServerValues

    private val _authServerSelect = MutableLiveData<KeyValueDAO>()
    val authServerSelect: LiveData<KeyValueDAO>
        get() = _authServerSelect

    init {
        fetchCount()
    }

    private fun fetchCount() {
        _authServerValues.value = configManager.authServersUrl
        _authServerSelect.value = configManager.readDaoAuthServerUrl()
    }

    fun onAuthServerSelected(keyValue: KeyValueDAO) {
        authServerValueSelected = keyValue
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
        deviceManager.doRestart()
    }

}
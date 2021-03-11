package com.wb.logistics.ui.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wb.logistics.ui.config.dao.KeyValueDao
import com.wb.logistics.utils.managers.ConfigManager
import com.wb.logistics.utils.managers.DeviceManager

class ConfigViewModel(
    private val configManager: ConfigManager,
    private val deviceManager: DeviceManager
) : ViewModel() {

    private var authServerValueSelected: KeyValueDao? = null
    private var autoSubstitutionChecked = false
    private var autoAuthorizationChecked = false

    private val _authServerValues = MutableLiveData<List<KeyValueDao>>()
    val authServerValues: LiveData<List<KeyValueDao>>
        get() = _authServerValues

    private val _authServerSelect = MutableLiveData<KeyValueDao>()
    val authServerSelect: LiveData<KeyValueDao>
        get() = _authServerSelect

    init {
        fetchCount()
    }

    private fun fetchCount() {
        _authServerValues.value = configManager.authServersUrl
        _authServerSelect.value = configManager.readDaoAuthServerUrl()
    }

    fun onAuthServerSelected(keyValue: KeyValueDao) {
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
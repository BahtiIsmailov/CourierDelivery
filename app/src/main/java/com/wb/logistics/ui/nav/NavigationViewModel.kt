package com.wb.logistics.ui.nav

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wb.logistics.ui.res.ResourceProvider
import com.wb.logistics.utils.managers.DeviceManager

class NavigationViewModel(
    private val resourceProvider: ResourceProvider,
    private val deviceManager: DeviceManager
) : ViewModel() {

    private val _countFlight = MutableLiveData<String>()
    val countFlight: LiveData<String>
        get() = _countFlight

    private val _versionApp = MutableLiveData<String>()
    val versionApp: LiveData<String>
        get() = _versionApp

    init {
        fetchCountFlight()
        fetchVersionApp()
    }

    private fun fetchCountFlight() {
        _countFlight.value = "1"
    }

    private fun fetchVersionApp() {
        _versionApp.value = resourceProvider.getVersionApp(deviceManager.appVersion)
    }

}
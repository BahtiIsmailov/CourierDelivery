package com.wb.logistics.ui.reception

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReceptionViewModel(
    private val receptionResourceProvider: ReceptionResourceProvider
) : ViewModel() {

    private val _codeBox = MutableLiveData<String>()
    val codeBox: LiveData<String>
        get() = _codeBox

    fun onBoxHandleInput(code: String) {
        _codeBox.value = receptionResourceProvider.getCodeBox(code)
    }

    fun onBoxScanned(code: String) {
        _codeBox.value = receptionResourceProvider.getCodeBox(code)
    }

}
package com.wb.logistics.ui.reception

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReceptionViewModel(
    private val receptionResourceProvider: ReceptionResourceProvider,
) : ViewModel() {

    val stateUI = MutableLiveData<ReceptionUIState<String>>()

    private val _codeBox = MutableLiveData<String>()
    val codeBox: LiveData<String>
        get() = _codeBox

    fun onBoxHandleInput(code: String) {
        //_codeBox.value = receptionResourceProvider.getCodeBox(code)
        stateUI.value = ReceptionUIState.NavigateToReceptionBoxNotBelong(code,
            "ПВЗ Москва, длинный адрес, который разошелся на 2 строки")
        stateUI.value = ReceptionUIState.Empty
    }

    fun onBoxScanned(code: String) {
        _codeBox.value = receptionResourceProvider.getCodeBox(code)
    }

}
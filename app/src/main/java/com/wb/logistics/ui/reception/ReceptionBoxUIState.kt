package com.wb.logistics.ui.reception

sealed class ReceptionBoxUIState<out R> {

    object Empty : ReceptionBoxUIState<Nothing>()

    data class BoxComplete(val countBox: String, val parking: String, val box: String) :
        ReceptionBoxUIState<Nothing>()

    data class BoxDeny(val countBox: String, val parking: String, val box: String) :
        ReceptionBoxUIState<Nothing>()

}
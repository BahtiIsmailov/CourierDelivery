package com.wb.logistics.ui.splash

sealed class AppVersionState {
    object UpToDate : AppVersionState()
    object UpToDateProgress : AppVersionState()
    data class Update(val fileName: String, val version: Int) : AppVersionState()
    object UpdateProgress : AppVersionState()
    data class UpdateComplete(val pathFile: String) : AppVersionState()
    object UpdateError : AppVersionState()
}
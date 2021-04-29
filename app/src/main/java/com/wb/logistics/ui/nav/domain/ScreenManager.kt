package com.wb.logistics.ui.nav.domain

interface ScreenManager {
    fun saveScreenState(screenManagerState: ScreenManagerState)
    fun readScreenState(): ScreenManagerState
    fun clear()
}
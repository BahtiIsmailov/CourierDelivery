package com.wb.logistics.ui.splash.domain

interface ScreenManager {
    fun saveScreenState(screenManagerState: ScreenManagerState)
    fun readScreenState(): ScreenManagerState
    fun clear()
}
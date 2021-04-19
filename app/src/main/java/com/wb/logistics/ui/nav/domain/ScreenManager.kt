package com.wb.logistics.ui.nav.domain

interface ScreenManager {
    fun saveScreenState(screen: ScreenState)
    fun readScreenState(): ScreenState
    fun clear()
}
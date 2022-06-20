package ru.wb.go.ui.app.domain

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface AppNavRepository {

    fun navigate(request: String)

    fun observeNavigation(): Flow<String>

}
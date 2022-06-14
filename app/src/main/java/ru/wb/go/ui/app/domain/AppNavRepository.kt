package ru.wb.go.ui.app.domain

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface AppNavRepository {

    suspend fun navigate(request: String)

    suspend fun observeNavigation(): Flow<String>

}
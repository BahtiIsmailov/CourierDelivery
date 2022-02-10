package ru.wb.go.ui.app.domain

import io.reactivex.Observable

interface AppNavRepository {

    fun navigate(request: String)

    fun observeNavigation(): Observable<String>

}
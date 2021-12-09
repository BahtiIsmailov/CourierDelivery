package ru.wb.go.ui.splash.domain

import io.reactivex.Observable

interface AppSharedRepository {

    fun search(request: String)

    fun observeSearch(): Observable<String>

}
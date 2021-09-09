package ru.wb.perevozka.ui.splash.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface AppSharedRepository {

    fun search(request: String)

    fun observeSearch(): Observable<String>

}
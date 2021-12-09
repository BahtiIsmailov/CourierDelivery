package ru.wb.go.ui.splash.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AppSharedRepositoryImpl : AppSharedRepository {

    private var searchSubject: PublishSubject<String> = PublishSubject.create()

    override fun search(request: String) {
        searchSubject.onNext(request)
    }

    override fun observeSearch(): Observable<String> {
        return searchSubject
    }

}
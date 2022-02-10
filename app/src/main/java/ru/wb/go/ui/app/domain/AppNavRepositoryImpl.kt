package ru.wb.go.ui.app.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class AppNavRepositoryImpl : AppNavRepository {

    private var searchSubject: PublishSubject<String> = PublishSubject.create()

    override fun navigate(request: String) {
        searchSubject.onNext(request)
    }

    override fun observeNavigation(): Observable<String> {
        return searchSubject
    }

}
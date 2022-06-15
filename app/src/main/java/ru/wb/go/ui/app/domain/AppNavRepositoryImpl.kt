package ru.wb.go.ui.app.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class AppNavRepositoryImpl : AppNavRepository {

    //private var searchSubject: PublishSubject<String> = PublishSubject.create()

    private var searchSubject = MutableSharedFlow<String>()

    override suspend fun navigate(request: String) {
        searchSubject.emit(request)
    }
    //    override fun navigate(request: String) {
//        searchSubject.onNext(request)
//    }

    override suspend fun observeNavigation(): Flow<String> {
        return searchSubject
    }
    //
//    override fun observeNavigation(): Observable<String> {
//        return searchSubject
//    }
//

    companion object{
        const val INVALID_TOKEN = "INVALID_TOKEN"
    }


//    companion object{
//        const val INVALID_TOKEN = "INVALID_TOKEN"
//    }

}
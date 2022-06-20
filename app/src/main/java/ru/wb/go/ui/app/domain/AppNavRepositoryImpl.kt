package ru.wb.go.ui.app.domain

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class AppNavRepositoryImpl : AppNavRepository {

    //private var searchSubject: PublishSubject<String> = PublishSubject.create()

    private var searchSubject = MutableSharedFlow<String>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun navigate(request: String) {
        searchSubject.tryEmit(request)
    }
    //    override fun navigate(request: String) {
//        searchSubject.onNext(request)
//    }

    override fun observeNavigation(): Flow<String> {
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
package ru.wb.perevozka.ui

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class NetworkViewModel(private val compositeDisposable: CompositeDisposable) :
    ViewModel() {

    protected fun addSubscription(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.apply { if (!isDisposed) dispose() }
    }

    fun clearSubscription() {
        compositeDisposable.apply { if (!isDisposed) clear() }
    }

}
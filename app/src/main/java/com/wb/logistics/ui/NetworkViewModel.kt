package com.wb.logistics.ui

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class NetworkViewModel(private val compositeDisposable: CompositeDisposable) : ViewModel() {

    protected fun addSubscription(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.apply {
            if (!isDisposed) {
                clear()
                dispose()
            }
        }
    }

}
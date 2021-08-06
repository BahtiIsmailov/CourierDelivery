package ru.wb.perevozka.ui

import androidx.lifecycle.ViewModel
import ru.wb.perevozka.utils.LogUtils
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class NetworkViewModel(private val compositeDisposable: CompositeDisposable) :
    ViewModel() {

    protected fun addSubscription(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        LogUtils { logDebugApp("SCOPE_DEBUG " + this@NetworkViewModel + " onCleared() NetworkViewModel") }
        compositeDisposable.apply {
            if (!isDisposed) {
                clear()
                dispose()
            }
        }
    }

}
package ru.wb.go.ui

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.wb.go.mvvm.BaseMessageResourceProvider
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToDialogInfo
import ru.wb.go.utils.analytics.YandexMetricManager

abstract class NetworkViewModel(
    private val compositeDisposable: CompositeDisposable,
    private val metric: YandexMetricManager,
) :
    ViewModel() {

    abstract fun getScreenTag(): String

    protected fun addSubscription(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        compositeDisposable.apply { if (!isDisposed) dispose() }
    }

    fun clearSubscription() {
        compositeDisposable.apply { if (!isDisposed) clear() }
    }

    fun onTechEventLog(method: String, message: String = EMPTY_MESSAGE) {
        metric.onTechEventLog(getScreenTag(), method, message)
    }

    fun onTechErrorLog(method: String, error: Throwable) {
        metric.onTechErrorLog(getScreenTag(), method, error.toString())
    }

    companion object {
        const val EMPTY_MESSAGE = ""
    }

}


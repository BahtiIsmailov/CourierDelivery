package ru.wb.go.ui

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ru.wb.go.mvvm.BaseMessageResourceProvider
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.dialogs.DialogStyle

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

    fun messageError(
        throwable: Throwable,
        resourceProvider: BaseMessageResourceProvider
    ): NavigateToDialogInfo {
        return when (throwable) {
            is NoInternetException -> NavigateToDialogInfo(
                DialogStyle.WARNING.ordinal,
                throwable.message,
                resourceProvider.getGenericInternetMessageError(),
                resourceProvider.getGenericInternetButtonError()
            )
            is BadRequestException -> NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                throwable.error.message,
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
            else -> NavigateToDialogInfo(
                DialogStyle.ERROR.ordinal,
                resourceProvider.getGenericServiceTitleError(),
                resourceProvider.getGenericServiceMessageError(),
                resourceProvider.getGenericServiceButtonError()
            )
        }

    }

    data class NavigateToDialogInfo(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    )

}


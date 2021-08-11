package ru.wb.perevozka.ui.userdata.userform

import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import ru.wb.perevozka.network.exceptions.BadRequestException
import ru.wb.perevozka.network.exceptions.NoInternetException
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.ui.NetworkViewModel
import ru.wb.perevozka.ui.SingleLiveEvent
import ru.wb.perevozka.ui.userdata.UserDataResourceProvider
import ru.wb.perevozka.ui.userdata.userform.domain.UserFormInteractor
import ru.wb.perevozka.utils.LogUtils
import java.util.*

class UserFormViewModel(
    private val parameters: UserFormParameters,
    compositeDisposable: CompositeDisposable,
    private val interactor: UserFormInteractor,
    private val resourceProvider: UserDataResourceProvider,
) : NetworkViewModel(compositeDisposable) {

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToMessageInfo>()
    val navigateToMessageInfo: LiveData<NavigateToMessageInfo>
        get() = _navigateToMessageInfo

    private val _toolbarNetworkState = MutableLiveData<NetworkState>()
    val toolbarNetworkState: LiveData<NetworkState>
        get() = _toolbarNetworkState

    private val _navigationEvent =
        SingleLiveEvent<UserFormNavAction>()
    val navigationEvent: LiveData<UserFormNavAction>
        get() = _navigationEvent

    private val _loaderState = MutableLiveData<UserFormUILoaderState>()
    val loaderState: LiveData<UserFormUILoaderState>
        get() = _loaderState

    init { observeNetworkState() }

    fun onTextChanges(changeObservables: ArrayList<Observable<Pair<String, UserFormQueryType>>>) {
        addSubscription(Observable.merge(changeObservables)
            //.distinct()
//            .map {
//                when (it.second) {
//                    UserFormQueryType.SURNAME -> it
//                    UserFormQueryType.NAME -> it
//                    UserFormQueryType.PATRONYMIC -> it
//                    UserFormQueryType.PASSPORT_SERIES -> {
//                        Pair(PassportUtils.seriesFormatter(it.first), it.second)
//                    }
//                    UserFormQueryType.PASSPORT_NUMBER -> it
//                    UserFormQueryType.PASSPORT_DATE -> it
//                    UserFormQueryType.PASSPORT_CODE ->
//                        Pair(PassportUtils.codeFormatter(it.first), it.second)
//                    UserFormQueryType.INN -> it
//                }
//            }
            //.doOnNext { _stateUI.value = UserFormUIState.ChangeDataField(it) }
            .doOnNext { LogUtils { logDebugApp(it.toString()) } }
            .scan(MutableList(UserFormQueryType.values().size) { 0 }, { accumulator, item ->
                accumulator[item.second.ordinal] =
                    when (item.second) {
                        UserFormQueryType.SURNAME -> if (item.first.isNotEmpty()) 1 else 0
                        UserFormQueryType.NAME -> if (item.first.isNotEmpty()) 1 else 0
                        UserFormQueryType.PATRONYMIC -> if (item.first.isNotEmpty()) 1 else 0
                        UserFormQueryType.PASSPORT_SERIES -> if (item.first.length == 4) 1 else 0
                        UserFormQueryType.PASSPORT_NUMBER -> if (item.first.length == 6) 1 else 0
                        UserFormQueryType.PASSPORT_DATE -> if (item.first.length == 10) 1 else 0
                        UserFormQueryType.PASSPORT_CODE -> if (item.first.length == 6) 1 else 0
                        UserFormQueryType.INN -> if (item.first.length == 12) 1 else 0
                    }
                return@scan accumulator
            })
            .map { it.sum() }
            .map { if (it == UserFormQueryType.values().size) UserFormUILoaderState.Enable else UserFormUILoaderState.Disable }
            .subscribe(
                {
                    _loaderState.value = it
//                    _loaderState.value = UserFormUILoaderState.Enable
                },
                {})
        )
    }

    fun onNextClick() {
        _loaderState.value = UserFormUILoaderState.Progress
        addSubscription(
            interactor.couriersForm(parameters.phone).subscribe(
                { couriersFormComplete() },
                { couriersFormError(it) })
        )
    }

    private fun couriersFormComplete() {
        _loaderState.value = UserFormUILoaderState.Disable
        _navigationEvent.value =
            UserFormNavAction.NavigateToCouriersCompleteRegistration(parameters.phone)
    }

    private fun couriersFormError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> "Сервис временно не доступен. Повторите операцию позднее"
        }
        _loaderState.value = UserFormUILoaderState.Enable
        _navigateToMessageInfo.value = NavigateToMessageInfo("Отправка данных", message, "OK")
    }

    private fun observeNetworkState() {
        addSubscription(
            interactor.observeNetworkConnected().subscribe({ _toolbarNetworkState.value = it }, {})
        )
    }

    data class NavigateToMessageInfo(val title: String, val message: String, val button: String)

}


data class InitTitle(val title: String, val phone: String)
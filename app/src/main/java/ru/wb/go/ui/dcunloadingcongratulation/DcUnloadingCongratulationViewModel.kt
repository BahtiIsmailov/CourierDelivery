package ru.wb.go.ui.dcunloadingcongratulation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.dcunloadedboxes.DcCongratulationEntity
import ru.wb.go.network.exceptions.BadRequestException
import ru.wb.go.network.exceptions.NoInternetException
import ru.wb.go.ui.NetworkViewModel
import ru.wb.go.ui.SingleLiveEvent
import ru.wb.go.ui.dcunloadingcongratulation.domain.DcUnloadingCongratulationInteractor
import ru.wb.go.ui.dialogs.DialogInfoStyle
import ru.wb.go.ui.dialogs.NavigateToInformation
import ru.wb.go.utils.managers.ScreenManager

class DcUnloadingCongratulationViewModel(
    compositeDisposable: CompositeDisposable,
    private val resourceProvider: DcUnloadingCongratulationResourceProvider,
    private val interactor: DcUnloadingCongratulationInteractor,
    private val screenManager: ScreenManager,
) : NetworkViewModel(compositeDisposable) {

    private val _infoState = MutableLiveData<InfoComplete>()
    val infoState: LiveData<InfoComplete>
        get() = _infoState

    private val _navigateToBack = MutableLiveData<NavigateToFlight>()
    val navigateToBack: LiveData<NavigateToFlight>
        get() = _navigateToBack

    private val _navigateToMessageInfo = SingleLiveEvent<NavigateToInformation>()
    val navigateToMessageInfo: LiveData<NavigateToInformation>
        get() = _navigateToMessageInfo

    init {
        addSubscription(
            interactor.congratulation().subscribe(
                congratulationComplete(), { congratulationError(it) })
        )
    }

    private fun congratulationComplete(): (t: DcCongratulationEntity) -> Unit =
        {
            with(it) {
                val delivered =
                    resourceProvider.getInfo(dcUnloadingCount, unloadingCount + dcUnloadingCount)
                val returned = resourceProvider.getInfo(
                    dcUnloadingReturnCount,
                    returnCount + dcUnloadingReturnCount
                )
                _infoState.value = InfoComplete(delivered, returned)
            }
        }

    private fun congratulationError(throwable: Throwable) {
        val message = when (throwable) {
            is NoInternetException -> throwable.message
            is BadRequestException -> throwable.error.message
            else -> resourceProvider.getScanDialogMessage()
        }
        _navigateToMessageInfo.value = NavigateToInformation(
            DialogInfoStyle.ERROR.ordinal,
            resourceProvider.getScanDialogTitle(), message, resourceProvider.getScanDialogButton()
        )
    }

    fun onCompleteClick() {
        screenManager.clear()
        _navigateToBack.value = NavigateToFlight
    }

    data class InfoComplete(val deliveredCount: String, val returnCount: String)
    object NavigateToFlight

}
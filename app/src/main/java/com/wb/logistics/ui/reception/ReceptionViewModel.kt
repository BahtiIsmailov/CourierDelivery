package com.wb.logistics.ui.reception

import androidx.lifecycle.MutableLiveData
import com.wb.logistics.ui.NetworkViewModel
import com.wb.logistics.ui.reception.domain.ReceptionBoxEntity
import com.wb.logistics.ui.reception.domain.ReceptionInteractor
import io.reactivex.disposables.CompositeDisposable

class ReceptionViewModel(
    compositeDisposable: CompositeDisposable,
    private val receptionResourceProvider: ReceptionResourceProvider,
    private val receptionInteractor: ReceptionInteractor,
) : NetworkViewModel(compositeDisposable) {

    val stateUI = MutableLiveData<ReceptionUIState<Nothing>>()

    val boxStateUI = MutableLiveData<ReceptionBoxUIState<Nothing>>()

    init {
        addSubscription(receptionInteractor.changeBoxes().subscribe({ changeBoxesComplete(it) },
            { changeBoxesError(it) }))
    }

    private fun changeBoxesComplete(boxes: List<ReceptionBoxEntity>) {
        if (boxes.isEmpty()) {
            boxStateUI.value = ReceptionBoxUIState.Empty
        } else {
            val count = boxes.size
            val lastBox = boxes.last().box
            if (count % 2 == 0) {
                boxStateUI.value =
                    ReceptionBoxUIState.BoxComplete(
                        receptionResourceProvider.getAddedBox(lastBox),
                        count.toString(),
                        "7",
                        lastBox)
            } else {
                boxStateUI.value = ReceptionBoxUIState.BoxDeny(count.toString(), "10", lastBox)
            }
        }
    }

    private fun changeBoxesError(error: Throwable) {

    }

    fun onBoxHandleInput(code: String) {
        // TODO: 30.03.2021 заменить на реальные данные
        //_codeBox.value = receptionResourceProvider.getCodeBox(code)
        stateUI.value = ReceptionUIState.NavigateToReceptionBoxNotBelong(
            receptionResourceProvider.getBoxNotBelongDcTitle(),
            code,
            receptionResourceProvider.getBoxNotBelongAddressTitle())
        stateUI.value = ReceptionUIState.Empty
    }

    fun onBoxScanned(code: String) {
        val formatCode = receptionResourceProvider.getCodeBox(code)
        receptionInteractor.saveBoxCode(formatCode, "ПВЗ Москва, ул. Карамазова, 32/3")
    }

    fun onListClicked() {
        stateUI.value = ReceptionUIState.NavigateToBoxes
        stateUI.value = ReceptionUIState.Empty
    }

}
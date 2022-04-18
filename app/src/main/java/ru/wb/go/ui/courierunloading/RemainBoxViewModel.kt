package ru.wb.go.ui.courierunloading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.ui.ServicesViewModel
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInteractor
import ru.wb.go.utils.analytics.YandexMetricManager


class RemainBoxViewModel(
    private val parameters: RemainBoxParameters,
    compositeDisposable: CompositeDisposable,
    metric: YandexMetricManager,
    private val resourceProvider: CourierUnloadingResourceProvider,
    private val interactor: CourierUnloadingInteractor,
) : ServicesViewModel(compositeDisposable, metric, interactor, resourceProvider) {

    private val _toolbarLabelState = MutableLiveData<Label>()
    val toolbarLabelState: LiveData<Label>
        get() = _toolbarLabelState

    private val _boxes = MutableLiveData<RemainBoxItemState>()
    val boxes: LiveData<RemainBoxItemState>
        get() = _boxes

    private var boxItems = mutableListOf<String>()

    init {
        initBoxes()
    }

    private fun initBoxes() {
        addSubscription(
            interactor.getRemainBoxes(parameters.officeId)
                .subscribe(
                    { fillRemainBoxList(it) },
                    { })
        )
    }

    private fun fillRemainBoxList(boxes: List<LocalBoxEntity>) {
        boxItems = boxes.map {
            "*".repeat(6) + " " + it.boxId.padStart(3, '0').takeLast(3)
        }.toMutableList()
        _boxes.value =
            if (boxItems.isEmpty()) RemainBoxItemState.Empty("")
            else RemainBoxItemState.InitItems(boxItems)
    }

    override fun getScreenTag(): String {
        return SCREEN_TAG
    }

    companion object {
        const val SCREEN_TAG = "RemainBox"
    }

    data class Label(val label: String)
}

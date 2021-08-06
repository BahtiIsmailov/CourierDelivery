package ru.wb.perevozka.utils.formatter

import com.jakewharton.rxbinding3.InitialValueObservable
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import kotlin.math.min

object BoxUtils {

    private const val TIME_OUT_DEBOUNCE = 25
    private const val MAX_BOX_FORMAT_DIGITS = 20
    private const val MAX_BOX_PREFIX = "TRBX"

    fun boxNumberFormatter(
        observableBox: InitialValueObservable<CharSequence>,
        rxSchedulerFactory: RxSchedulerFactory,
    ): Observable<String> {
        return observableBox
            .map { it.toString() }
            .debounce(TIME_OUT_DEBOUNCE.toLong(), TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .map { boxNumberFormat(it) }
            .map {
                it.substring(0, min(it.length, MAX_BOX_FORMAT_DIGITS))
            }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    private fun boxNumberFormat(boxNumber: String): String {
        val formatNumber = StringBuilder()
        val boxDigits = getBoxDigits(boxNumber)
        formatNumber.append(MAX_BOX_PREFIX)
        formatNumber.append(boxDigits)
        return formatNumber.toString()
    }

    private fun getBoxDigits(boxNumber: String): String {
        return boxNumber.filter { it.isDigit() }
    }

}
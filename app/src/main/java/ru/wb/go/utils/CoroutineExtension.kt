package ru.wb.go.utils

import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.annotation.CheckResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.conflate
import java.util.concurrent.TimeUnit

object CoroutineExtension {

    fun interval(time: Long, timeUnit: TimeUnit): Flow<Long> = flow {

        var counter: Long = 0

        val delayTime = when (timeUnit) {
            TimeUnit.MICROSECONDS -> time / 1000
            TimeUnit.NANOSECONDS -> time / 1_000_000
            TimeUnit.SECONDS -> time * 1000
            TimeUnit.MINUTES -> 60 * time * 1000
            TimeUnit.HOURS -> 60 * 60 * time * 1000
            TimeUnit.DAYS -> 24 * 60 * 60 * time * 1000
            else -> time
        }

        while (true) {
            delay(delayTime)
            emit(counter++)
        }

    }
}

@CheckResult
fun TextView.textChanges(): InitialValueFlow<CharSequence> = callbackFlow {
    checkMainThread()
    val listener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) =
            Unit

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            trySend(s)
        }

        override fun afterTextChanged(s: Editable) = Unit
    }

    addTextChangedListener(listener)
    awaitClose { removeTextChangedListener(listener) }
}
    .conflate()
    .asInitialValueFlow { text }



@CheckResult
fun View.clicks(): Flow<Unit> = callbackFlow {
    checkMainThread()
    val listener = View.OnClickListener {
        trySend(Unit)
    }
    setOnClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.conflate()


fun checkMainThread() {
    check(Looper.myLooper() == Looper.getMainLooper()) {
        "Expected to be called on the main thread but was " + Thread.currentThread().name
    }
}

@CheckResult
fun View.focusChanges(): InitialValueFlow<Boolean> = callbackFlow {
    checkMainThread()
    val listener = View.OnFocusChangeListener { _, hasFocus ->
        trySend(hasFocus)
    }
    onFocusChangeListener = listener
    awaitClose { onFocusChangeListener = null }
}
    .conflate()
    .asInitialValueFlow { hasFocus() }



private fun <T : Any> Flow<T>.asInitialValueFlow(initialValue: () -> T): InitialValueFlow<T> = InitialValueFlow(
    onStart {
        emit(initialValue())
    }
)

class InitialValueFlow<T : Any>(private val flow: Flow<T>) : Flow<T> by flow {

      fun skipInitialValue(): Flow<T> = flow.drop(1)
}

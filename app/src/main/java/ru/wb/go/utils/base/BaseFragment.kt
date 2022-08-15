package ru.wb.go.utils.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import ru.wb.go.utils.Event
import ru.wb.go.utils.EventObserver

abstract class BaseFragment : Fragment() {

    fun <T> Flow<T>.observeEvent(observer: (T) -> Unit) {
        lifecycleScope.launchWhenStarted {
            this@observeEvent.collect { event ->
                observer.invoke(event)
            }

        }
    }

    fun<T> LiveData<T>.observe(observer: (T) -> Unit) {
        this.observe(viewLifecycleOwner, observer)
    }
}
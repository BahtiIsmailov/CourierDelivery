package ru.wb.go.utils.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import ru.wb.go.utils.Event
import ru.wb.go.utils.EventObserver

abstract class BaseFragment : Fragment() {

    fun<T> LiveData<Event<T>>.observeEvent(observer: (T) -> Unit) {
        this.observe(viewLifecycleOwner, EventObserver{
            observer(it)
        })
    }
}
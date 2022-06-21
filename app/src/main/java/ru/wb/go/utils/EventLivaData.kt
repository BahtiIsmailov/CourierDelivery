package ru.wb.go.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

open class Event<out T>(private val content: T) {

    @Suppress("MemberVisibilityCanBePrivate")
    var hasBeenHandled = false
        private set // Allow external read but not write

    /* Returns the content and prevents its use again.
    */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /* Returns the content, even if it's already been handled.
    */
    fun peekContent(): T = content
}
typealias NoParamsMutableEvent = MutableLiveData<Event<Unit>>
typealias NoParamsEvent = LiveData<Event<Unit>>

/* An [Observer] for [Event]s, simplifying the pattern of checking if the [Event]'s content has
* already been handled.
*
* [onEventUnhandledContent] is *only* called if the [Event]'s contents has not been handled.
*/
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let {
            onEventUnhandledContent(it)
        }
    }
}

fun <T> MutableLiveData<Event<T>>.emitEvent(content: T){
    value = Event(content)
}

fun MutableLiveData<Event<Unit>>.emitEvent(){
    value = Event(Unit)
}

fun <T> MutableLiveData<T>.emit(content: T){
    value = content
}
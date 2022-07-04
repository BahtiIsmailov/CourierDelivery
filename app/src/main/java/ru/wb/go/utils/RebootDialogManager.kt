package ru.wb.go.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object RebootDialogManager {

    private val showRebootDialogFlow = Channel<Int>(
         onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun showRebootDialog(code:Int){
        showRebootDialogFlow.trySend(code)
    }

    fun observerShowRebootDialog(): Flow<Int> {
        return showRebootDialogFlow.receiveAsFlow()
    }
}
package ru.wb.go.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

object RebootDialogManager {

    private val showRebootDialogFlow = Channel<Unit>(
         onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun showRebootDialog(){
        showRebootDialogFlow.trySend(Unit)
    }

    fun observerShowRebootDialog(): Flow<Unit> {
        return showRebootDialogFlow.receiveAsFlow()
    }
}
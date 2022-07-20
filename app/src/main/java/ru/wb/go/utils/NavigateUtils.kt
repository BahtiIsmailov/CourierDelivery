package ru.wb.go.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

object NavigateUtils {

    private var navigateUtilsSharedFlow = MutableSharedFlow<String>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun setDataToNavigateUtilsSharedFlow(fragmentName:String){
        navigateUtilsSharedFlow.tryEmit(fragmentName)
    }

    fun getDataNavigateUtilsSharedFlow():Flow<String>{
        return navigateUtilsSharedFlow
    }

    fun clearNavigateUtilsSharedFlow(){
        navigateUtilsSharedFlow = MutableSharedFlow(
            extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }
}
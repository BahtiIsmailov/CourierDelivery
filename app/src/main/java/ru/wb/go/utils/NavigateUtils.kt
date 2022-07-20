package ru.wb.go.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object NavigateUtils {

    private var navigateUtilsSharedFlow = MutableStateFlow("")
    var test:String? = null

    fun setDataToNavigateUtilsSharedFlow(fragmentName:String){
        navigateUtilsSharedFlow.tryEmit(fragmentName)
    }


    fun getDataNavigateUtilsSharedFlow(): Flow<String> {
        return navigateUtilsSharedFlow.asStateFlow()
    }

    fun clearNavigateUtilsSharedFlow(){
        //navigateUtilsSharedFlow = Channel(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }
}
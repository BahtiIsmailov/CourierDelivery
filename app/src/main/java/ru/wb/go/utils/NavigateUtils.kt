package ru.wb.go.utils

import kotlinx.coroutines.flow.MutableStateFlow

object NavigateUtils {

    private var navigateUtilsSharedFlow = MutableStateFlow("")
    var test:String? = null

    fun setDataToNavigateUtilsSharedFlow(fragmentName:String){
        navigateUtilsSharedFlow.tryEmit(fragmentName)
    }


    fun getDataNavigateUtilsSharedFlow():String{
        return navigateUtilsSharedFlow.value
    }

    fun clearNavigateUtilsSharedFlow(){
        //navigateUtilsSharedFlow = Channel(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }
}
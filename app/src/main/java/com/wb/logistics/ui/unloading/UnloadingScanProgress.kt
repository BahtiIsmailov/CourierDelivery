package com.wb.logistics.ui.unloading

sealed class UnloadingScanProgress {

    object LoaderProgress : UnloadingScanProgress()

    object LoaderComplete : UnloadingScanProgress()

}
package com.wb.logistics.ui.unloadingscan

sealed class UnloadingScanProgress {

    object LoaderProgress : UnloadingScanProgress()

    object LoaderComplete : UnloadingScanProgress()

}
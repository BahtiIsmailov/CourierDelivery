package ru.wb.go.ui.unloadingscan

sealed class UnloadingScanProgress {

    object LoaderProgress : UnloadingScanProgress()

    object LoaderComplete : UnloadingScanProgress()

}
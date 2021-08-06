package ru.wb.perevozka.ui.unloadingscan

sealed class UnloadingScanProgress {

    object LoaderProgress : UnloadingScanProgress()

    object LoaderComplete : UnloadingScanProgress()

}
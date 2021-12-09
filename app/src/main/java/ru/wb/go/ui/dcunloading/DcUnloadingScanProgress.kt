package ru.wb.go.ui.dcunloading

sealed class DcUnloadingScanProgress {

    object LoaderProgress : DcUnloadingScanProgress()

    object LoaderComplete : DcUnloadingScanProgress()

}
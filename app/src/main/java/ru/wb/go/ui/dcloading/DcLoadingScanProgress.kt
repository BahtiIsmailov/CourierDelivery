package ru.wb.go.ui.dcloading

sealed class DcLoadingScanProgress {

    object LoaderProgress : DcLoadingScanProgress()

    object LoaderComplete : DcLoadingScanProgress()

}
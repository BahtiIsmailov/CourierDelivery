package ru.wb.perevozka.ui.dcloading

sealed class DcLoadingScanProgress {

    object LoaderProgress : DcLoadingScanProgress()

    object LoaderComplete : DcLoadingScanProgress()

}
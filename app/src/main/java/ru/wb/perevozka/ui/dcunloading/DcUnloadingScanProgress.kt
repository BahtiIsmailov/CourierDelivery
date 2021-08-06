package ru.wb.perevozka.ui.dcunloading

sealed class DcUnloadingScanProgress {

    object LoaderProgress : DcUnloadingScanProgress()

    object LoaderComplete : DcUnloadingScanProgress()

}
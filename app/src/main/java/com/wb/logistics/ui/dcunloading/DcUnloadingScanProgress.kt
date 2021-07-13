package com.wb.logistics.ui.dcunloading

sealed class DcUnloadingScanProgress {

    object LoaderProgress : DcUnloadingScanProgress()

    object LoaderComplete : DcUnloadingScanProgress()

}
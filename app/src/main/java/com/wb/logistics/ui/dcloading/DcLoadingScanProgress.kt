package com.wb.logistics.ui.dcloading

sealed class DcLoadingScanProgress {

    object LoaderProgress : DcLoadingScanProgress()

    object LoaderComplete : DcLoadingScanProgress()

}
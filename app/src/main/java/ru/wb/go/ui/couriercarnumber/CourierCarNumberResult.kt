package ru.wb.go.ui.couriercarnumber

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class CourierCarNumberResult: Parcelable {

    @Parcelize data class Edit(val id: Int) : CourierCarNumberResult()

    @Parcelize data class Create(val id: Int) : CourierCarNumberResult()

}
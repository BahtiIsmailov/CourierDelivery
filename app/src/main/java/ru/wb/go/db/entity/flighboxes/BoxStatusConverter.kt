package ru.wb.go.db.entity.flighboxes

import androidx.room.TypeConverter

class BoxStatusConverter {

    @TypeConverter
    fun toBoxStatus(value: String) = enumValueOf<BoxStatus>(value)

    @TypeConverter
    fun fromBoxStatus(value: BoxStatus) = value.name

}
package ru.wb.perevozka.network.api.app.entity

data class CourierDocumentsEntity(
    val firstName: String,
    val surName: String,
    val middleName: String,
    val inn: String,
    val passportSeries: String,
    val passportNumber: String,
    val passportDateOfIssue: String,
)
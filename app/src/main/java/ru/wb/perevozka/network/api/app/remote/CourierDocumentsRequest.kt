package ru.wb.perevozka.network.api.app.remote

data class CourierDocumentsRequest(
    val firstName: String,
    val surName: String,
    val middleName: String,
    val inn: String,
    val passportSeries: String,
    val passportNumber: String,
    val passportDateOfIssue: String,
    val passportIssuedBy: String,
    val passportDepartmentCode: String,
)
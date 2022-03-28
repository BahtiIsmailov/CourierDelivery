package ru.wb.go.network.api.app.remote.courier

data class CourierDocumentsResponse(
    val errorAnnotate: String?,
    val firstName: String,
    val inn: String,
    val middleName: String,
    val passportDateOfIssue: String,
    val passportDepartmentCode: String,
    val passportIssuedBy: String,
    val passportNumber: String,
    val passportSeries: String,
    val surName: String,
    val courierType: String,
)
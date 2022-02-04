package ru.wb.go.network.api.app.entity

import kotlinx.serialization.Serializable

@Serializable
data class CourierDocumentsEntity(
    val firstName: String = "",
    val surName: String = "",
    val middleName: String = "",
    val inn: String = "",
    val passportSeries: String = "",
    val passportNumber: String = "",
    val passportDateOfIssue: String = "",
    val passportIssuedBy: String = "",
    val passportDepartmentCode: String = "",
    val errorAnnotate: String? = null,
)
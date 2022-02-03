package ru.wb.go.network.token

data class Extra(
    val employeeID: Int = 0,
    val phone: String = "",
    val wbUserID: Long = 0,
    val company: String? = "",
    val companyID: Int = 0,
    val place: String = "",
    val position: String = "",
    val positionID: Int = 0,
    val resources: List<String>? = listOf()
)

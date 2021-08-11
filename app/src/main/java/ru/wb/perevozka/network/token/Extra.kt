package ru.wb.perevozka.network.token

data class Extra(
    val employeeID: Int,
    val phone: String,
    val wbUserID: Long,
    val company: String,
    val companyID: Int,
    val place: String,
    val position: String,
    val positionID: Int,
    val resources: List<String>?
)

package ru.wb.perevozka.ui.auth.domain

sealed class CheckSmsData {
    object NeedSendCourierDocument : CheckSmsData()
    object NeedApproveCourierDocuments : CheckSmsData()
    object UserRegistered : CheckSmsData()
}
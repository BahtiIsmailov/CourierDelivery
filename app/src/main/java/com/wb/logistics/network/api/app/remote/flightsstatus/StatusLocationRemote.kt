package com.wb.logistics.network.api.app.remote.flightsstatus

data class StatusLocationRemote(
    val office: StatusOfficeLocationRemote?,
    val getFromGPS: Boolean?,
)


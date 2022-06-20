package ru.wb.go.network.api.refreshtoken

interface RefreshTokenRepository {

       suspend fun doRefreshToken():RefreshResult

}

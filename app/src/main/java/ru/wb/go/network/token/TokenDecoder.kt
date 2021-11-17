package ru.wb.go.network.token

import android.util.Base64
import com.google.gson.Gson

fun decodeToken(accessToken: String): TokenResource =
    Gson().fromJson(
        String(Base64.decode(accessToken.split(".")[1], Base64.URL_SAFE)),
        TokenResource::class.java
    )

package ru.wb.perevozka.di.module

import ru.wb.perevozka.network.api.app.AppApi
import ru.wb.perevozka.network.api.auth.AuthApi
import ru.wb.perevozka.network.headers.RefreshTokenApi
import ru.wb.perevozka.network.rest.RefreshTokenRetrofitFactory
import ru.wb.perevozka.network.rest.RetrofitFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module

val apiModule = module {

    fun provideAuthApi(retrofitFactory: RetrofitFactory): AuthApi {
        return retrofitFactory.getApiInterface(AuthApi::class.java)
    }

    fun provideRefreshTokenApi(retrofitFactory: RefreshTokenRetrofitFactory): RefreshTokenApi {
        return retrofitFactory.getApiInterface(RefreshTokenApi::class.java)
    }

    fun provideAppApi(retrofitFactory: RetrofitFactory): AppApi {
        return retrofitFactory.getApiInterface(AppApi::class.java)
    }

    single { provideAuthApi(get(named(AUTH_NAMED_RETROFIT))) }
    single { provideRefreshTokenApi(get(named(REFRESH_TOKEN_NAMED_RETROFIT))) }
    single { provideAppApi(get(named(APP_NAMED_RETROFIT))) }

}
package com.wb.logistics.di.module

import com.wb.logistics.network.api.app.RemoteAppRepository
import com.wb.logistics.network.api.auth.AuthApi
import com.wb.logistics.network.headers.RefreshTokenApi
import com.wb.logistics.network.rest.RefreshTokenRetrofitFactory
import com.wb.logistics.network.rest.RetrofitFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module

val apiModule = module {

    fun provideAuthApi(retrofitFactory: RetrofitFactory): AuthApi {
        return retrofitFactory.getApiInterface(AuthApi::class.java)
    }

    fun provideRefreshTokenApi(retrofitFactory: RefreshTokenRetrofitFactory): RefreshTokenApi {
        return retrofitFactory.getApiInterface(RefreshTokenApi::class.java)
    }

    fun provideAppApi(retrofitFactory: RetrofitFactory): RemoteAppRepository {
        return retrofitFactory.getApiInterface(RemoteAppRepository::class.java)
    }

    single { provideAuthApi(get(named(AUTH_NAMED_RETROFIT))) }
    single { provideRefreshTokenApi(get(named(REFRESH_TOKEN_NAMED_RETROFIT))) }
    single { provideAppApi(get(named(APP_NAMED_RETROFIT))) }

}
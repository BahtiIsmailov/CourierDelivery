package ru.wb.go.di.module

import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.wb.go.network.api.app.AppApi
import ru.wb.go.network.api.app.AppTasksApi
import ru.wb.go.network.api.auth.AuthApi
import ru.wb.go.network.headers.RefreshTokenApi
import ru.wb.go.network.rest.RefreshTokenRetrofitFactory
import ru.wb.go.network.rest.RetrofitFactory

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

    fun provideAppTasksApi(retrofitFactory: RetrofitFactory): AppTasksApi {
        return retrofitFactory.getApiDynamicInterface(AppTasksApi::class.java)
    }

    single { provideAuthApi(get(named(AUTH_NAMED_RETROFIT))) }
    single { provideRefreshTokenApi(get(named(REFRESH_TOKEN_NAMED_RETROFIT))) }
    single { provideAppApi(get(named(APP_NAMED_RETROFIT))) }
    single { provideAppTasksApi(get(named(APP_NAMED_TASKS_RETROFIT))) }

}
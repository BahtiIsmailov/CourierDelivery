package ru.wb.perevozka.ui.userdata.couriers.domain

import io.reactivex.Single
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.headers.RefreshTokenRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager

class CouriersCompleteRegistrationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val appLocalRepository: AppLocalRepository,
    private val authRepository: AuthRemoteRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenManager: TokenManager
) : CouriersCompleteRegistrationInteractor {

    override fun isRegisteredStatus(): Single<Boolean> {
        val refreshToken = refreshTokenRepository.refreshAccessTokensSync()
        val isEmptyTokenResources = Single.fromCallable { tokenManager.resources().isEmpty() }
        return refreshToken.andThen(isEmptyTokenResources)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
package ru.wb.perevozka.ui.courierexpects.domain

import io.reactivex.Single
import ru.wb.perevozka.app.COURIER_ROLE
import ru.wb.perevozka.network.headers.RefreshTokenRepository
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.network.token.TokenManager

class CourierExpectsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenManager: TokenManager
) : CourierExpectsInteractor {

    override fun isRegisteredStatus(): Single<Boolean> {
        val refreshToken = refreshTokenRepository.refreshAccessTokensSync()
        val isEmptyTokenResources =
            Single.fromCallable { tokenManager.resources().contains(COURIER_ROLE) }
        return refreshToken.andThen(isEmptyTokenResources)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
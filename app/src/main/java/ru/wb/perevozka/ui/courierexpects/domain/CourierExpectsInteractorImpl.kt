package ru.wb.perevozka.ui.courierexpects.domain

import io.reactivex.Single
import ru.wb.perevozka.app.COURIER_ROLE
import ru.wb.perevozka.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.perevozka.app.NEED_SEND_COURIER_DOCUMENTS
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
            Single.fromCallable {
                !tokenManager.resources().contains(NEED_SEND_COURIER_DOCUMENTS) &&
                        !tokenManager.resources().contains(NEED_APPROVE_COURIER_DOCUMENTS)
            }
        return refreshToken.andThen(isEmptyTokenResources)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

}
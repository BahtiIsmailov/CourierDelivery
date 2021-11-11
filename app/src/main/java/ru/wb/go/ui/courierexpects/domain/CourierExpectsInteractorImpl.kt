package ru.wb.go.ui.courierexpects.domain

import io.reactivex.Single
import ru.wb.go.app.COURIER_ROLE
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.headers.RefreshTokenRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager

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
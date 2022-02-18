package ru.wb.go.ui.courierexpects.domain

import io.reactivex.Single
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.api.refreshtoken.RefreshResult
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.ui.app.domain.AppNavRepositoryImpl.Companion.INVALID_TOKEN

class CourierExpectsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenManager: TokenManager
) : CourierExpectsInteractor {

    override fun isRegisteredStatus(): Single<String> {

        val regStatus = Single.fromCallable {
            val refreshResult = refreshTokenRepository.doRefreshToken()
            val resource = tokenManager.resources()
            when {
                refreshResult == RefreshResult.TokenInvalid -> INVALID_TOKEN
                resource.contains(NEED_SEND_COURIER_DOCUMENTS) -> {
                    NEED_SEND_COURIER_DOCUMENTS
                }
                resource.contains(NEED_CORRECT_COURIER_DOCUMENTS) -> {
                    NEED_CORRECT_COURIER_DOCUMENTS
                }
                resource.contains(NEED_APPROVE_COURIER_DOCUMENTS) -> {
                    NEED_APPROVE_COURIER_DOCUMENTS
                }
                else -> {
                    ""
                }
            }
        }
        return regStatus
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }


}
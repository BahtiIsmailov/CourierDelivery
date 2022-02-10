package ru.wb.go.ui.courierexpects.domain

import io.reactivex.Single
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager

class CourierExpectsInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenManager: TokenManager
) : CourierExpectsInteractor {

    override fun isRegisteredStatus(): Single<String> {
        val refreshToken = refreshTokenRepository.refreshAccessToken()
        val registrationToken =
            Single.fromCallable {
                val resource = tokenManager.resources()
                //TODO WHEN not work
                if (resource.contains(NEED_SEND_COURIER_DOCUMENTS)) {
                    NEED_SEND_COURIER_DOCUMENTS
                } else if (resource.contains(NEED_CORRECT_COURIER_DOCUMENTS)) {
                    NEED_CORRECT_COURIER_DOCUMENTS
                } else if (resource.contains(NEED_APPROVE_COURIER_DOCUMENTS)) {
                    NEED_APPROVE_COURIER_DOCUMENTS
                } else{""}


            }
        return refreshToken.andThen(registrationToken)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }


}
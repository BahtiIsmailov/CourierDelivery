package ru.wb.go.ui.courierdataexpects.domain

import ru.wb.go.app.INTERNAL_SERVER_ERROR_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_APPROVE_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_CORRECT_COURIER_DOCUMENTS
import ru.wb.go.app.NEED_SEND_COURIER_DOCUMENTS
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.refreshtoken.RefreshResult
import ru.wb.go.network.api.refreshtoken.RefreshTokenRepository
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.app.domain.AppNavRepositoryImpl.Companion.INVALID_TOKEN

class CourierDataExpectsInteractorImpl(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val tokenManager: TokenManager,
    private val userManager: UserManager,
) : CourierDataExpectsInteractor {

    override suspend fun saveRepeatCourierDocuments() {
        val courierDocumentsEntity = userManager.courierDocumentsEntity()
        if (courierDocumentsEntity != null) {
            appRemoteRepository.saveCourierDocuments(courierDocumentsEntity)
            userManager.clearCourierDocumentsEntity()
        }
    }

    override suspend fun isRegisteredStatus(): String {
        return if (userManager.courierDocumentsEntity() == null) {
            val refreshResult = refreshTokenRepository.doRefreshToken() // сюда приходит нетворк он майн срэд эксепшн
            val resource = tokenManager.resources()// сюда приходит NEED_SEND_COURIER_DOCUMENTS
            when {
                refreshResult == RefreshResult.TokenInvalid -> INVALID_TOKEN // сначало срабатывает тут и должно тут и оставваться
                resource.contains(NEED_SEND_COURIER_DOCUMENTS) -> NEED_SEND_COURIER_DOCUMENTS // потом прыгает сюда и дальше
                resource.contains(NEED_CORRECT_COURIER_DOCUMENTS) -> NEED_CORRECT_COURIER_DOCUMENTS
                resource.contains(NEED_APPROVE_COURIER_DOCUMENTS) -> NEED_APPROVE_COURIER_DOCUMENTS
                else -> ""
            }
        } else INTERNAL_SERVER_ERROR_COURIER_DOCUMENTS


    }
}

/*
   override fun saveRepeatCourierDocuments(): Completable {
       val courierDocumentsEntity = userManager.courierDocumentsEntity()
       return if (courierDocumentsEntity == null) Completable.complete()
       else appRemoteRepository.saveCourierDocuments(courierDocumentsEntity)
           .doOnComplete { userManager.clearCourierDocumentsEntity() }
           .compose(rxSchedulerFactory.applyCompletableSchedulers())
   }

   override fun isRegisteredStatus(): Single<String> {
       return Single.fromCallable {
           if (userManager.courierDocumentsEntity() == null) {
               val refreshResult = refreshTokenRepository.doRefreshToken()
               val resource = tokenManager.resources()
               when {
                   refreshResult == RefreshResult.TokenInvalid -> INVALID_TOKEN
                   resource.contains(NEED_SEND_COURIER_DOCUMENTS) -> NEED_SEND_COURIER_DOCUMENTS
                   resource.contains(NEED_CORRECT_COURIER_DOCUMENTS) -> NEED_CORRECT_COURIER_DOCUMENTS
                   resource.contains(NEED_APPROVE_COURIER_DOCUMENTS) -> NEED_APPROVE_COURIER_DOCUMENTS
                   else -> ""
               }
           } else INTERNAL_SERVER_ERROR_COURIER_DOCUMENTS
       }.compose(rxSchedulerFactory.applySingleSchedulers())
   }

 */
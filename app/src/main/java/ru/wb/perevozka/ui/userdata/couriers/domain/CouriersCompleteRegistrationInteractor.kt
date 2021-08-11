package ru.wb.perevozka.ui.userdata.couriers.domain

import io.reactivex.Single

interface CouriersCompleteRegistrationInteractor {

    fun isRegisteredStatus(): Single<Boolean>

}
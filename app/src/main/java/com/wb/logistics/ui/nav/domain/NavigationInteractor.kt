package com.wb.logistics.ui.nav.domain

import io.reactivex.Single

interface NavigationInteractor {
    fun sessionInfo(): Single<Pair<String, String>>
}
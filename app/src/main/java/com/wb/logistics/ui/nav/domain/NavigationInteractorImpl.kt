package com.wb.logistics.ui.nav.domain

import com.wb.logistics.network.api.auth.AuthRepository
import io.reactivex.Single

class NavigationInteractorImpl(
    private val repository: AuthRepository
) : NavigationInteractor {
    override fun sessionInfo(): Single<Pair<String, String>> {
        return repository.userInfo()
    }
}
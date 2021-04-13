package com.wb.logistics.network.token

import com.wb.logistics.app.AppPreffsKeys
import com.wb.logistics.utils.prefs.SharedWorker

class UserManagerImpl(private val worker: SharedWorker) : UserManager {

    override fun isUserChanged(phone: String): Boolean {
        return worker.load(AppPreffsKeys.PHONE_KEY, "") != phone
    }

    override fun savePhone(phone: String) {
        worker.save(AppPreffsKeys.PHONE_KEY, phone)
    }

    override fun clear() {
        worker.delete(AppPreffsKeys.PHONE_KEY)
    }

}
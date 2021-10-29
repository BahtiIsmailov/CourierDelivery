package ru.wb.perevozka.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.network.api.app.entity.CourierBillingAccountEntity

@Dao
interface CourierAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(courierBillingAccountEntity: CourierBillingAccountEntity): Completable

    @Query("SELECT * FROM CourierBillingAccountEntity")
    fun readAllAccount(): Single<List<CourierBillingAccountEntity>>

    @Query("SELECT * FROM CourierBillingAccountEntity WHERE account = :account")
    fun readAccount(account: String): Single<CourierBillingAccountEntity>

    @Query("DELETE FROM CourierBillingAccountEntity WHERE account IN (:account)")
    fun deleteAccountByAccount(account: String): Completable

    @Query("DELETE FROM CourierBillingAccountEntity")
    fun deleteAllAccount(): Completable

}
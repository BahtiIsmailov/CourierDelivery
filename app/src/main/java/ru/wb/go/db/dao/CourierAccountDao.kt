package ru.wb.go.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.wb.go.network.api.app.entity.CourierBillingAccountEntity

@Dao
interface CourierAccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccount(courierBillingAccountEntity: CourierBillingAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAccounts(courierBillingAccountEntities: List<CourierBillingAccountEntity>)

    @Query("SELECT * FROM CourierBillingAccountEntity")
    fun readAllAccount():  List<CourierBillingAccountEntity>

    @Query("SELECT * FROM CourierBillingAccountEntity WHERE correspondentAccount = :account")
    fun readAccount(account: String): CourierBillingAccountEntity

    @Query("DELETE FROM CourierBillingAccountEntity WHERE correspondentAccount IN (:account)")
    fun deleteAccountByAccount(account: String)

    @Query("DELETE FROM CourierBillingAccountEntity")
    fun deleteAllAccount()

}
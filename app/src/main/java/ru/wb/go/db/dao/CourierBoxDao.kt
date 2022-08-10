package ru.wb.go.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalLoadingBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalOfficeEntity
import ru.wb.go.ui.courierunloading.data.FakeBeep

@Dao
interface CourierBoxDao {

    @Query("SELECT * FROM boxes")
    suspend fun readAllBoxesSync(): List<LocalBoxEntity>

    @Insert
    suspend fun addBox(box: LocalBoxEntity)

    @Query(
        """
        UPDATE offices 
        SET count_boxes=(
            SELECT count(*)
            FROM boxes as b
            WHERE b.office_id = offices.office_id
            )
        WHERE office_id =:officeId
        """
    )
    suspend fun updateOfficeCountersAfterLoadingBox(officeId: Int)

    @Query("SELECT address AS address, count(*) AS count FROM boxes GROUP BY office_id")
    suspend fun loadingBoxBoxesGroupByOffice(): List<LocalLoadingBoxEntity>

    @Query(
        """
        UPDATE offices 
        SET delivered_boxes=(
            SELECT count(*)
            FROM boxes as b
            WHERE b.office_id = offices.office_id
            AND b.delivered_at<>''
            ),
            is_online = 0            
        WHERE office_id =:officeId
        """
    )
    suspend fun updateOfficeDeliveredBoxAfterUnload(officeId: Int)

    @Transaction
    suspend fun addNewBox(box: LocalBoxEntity) {
        addBox(box)
        updateOfficeCountersAfterLoadingBox(box.officeId)
    }

    @Query("UPDATE boxes SET loading_at=:loadingAt WHERE box_id=:boxId")
    suspend fun updateBoxLoadingAt(boxId: String, loadingAt: String)

    @Insert
    suspend fun addBoxes(boxes: List<LocalBoxEntity>)

    @Query("DELETE FROM boxes")
    suspend fun deleteBoxes()

    @Query("SELECT * FROM boxes")
    suspend fun getBoxes(): List<LocalBoxEntity>

    @Query("SELECT * FROM boxes WHERE fake_office_id <> '' ")
    suspend fun getFailedBoxes():List<LocalBoxEntity>

    @Query("SELECT delivered_at FROM BOXES WHERE box_id=:boxId")
    suspend fun isBoxesExist(boxId: String):List<String>

    @Query("UPDATE boxes SET delivered_at='' WHERE box_id=:boxId ")
    fun clearDelivery(boxId: String)

    @Transaction
    suspend fun setTransactionToFailedBoxes(fakeOfficeId: Int, loadingAt: String,boxId: String,officeId: Int) {
        setFailedBoxes(fakeOfficeId,loadingAt,boxId)
        setFailedDataToTableOffice(officeId)
    }

    @Query("UPDATE BOXES SET fake_office_id=:fakeOfficeId, fake_delivered_at=:loadingAt  WHERE box_id=:boxId")
    suspend fun setFailedBoxes(fakeOfficeId: Int, loadingAt: String,boxId: String)

    @Query("""UPDATE offices set fake_delivery_at =
            (select fake_delivered_at from boxes as b where b.office_id = :officeId), 
            fake_office_id = (select fake_office_id from boxes as b where b.office_id = :officeId),is_online = 0, is_visited=1 
            WHERE office_id =:officeId""")
    suspend fun setFailedDataToTableOffice(officeId: Int)

    @Query("SELECT * FROM OFFICES")
    suspend fun getOffices():List<LocalOfficeEntity>

    @Query("SELECT * FROM boxes")
    fun getBoxesLive(): Flow<List<LocalBoxEntity>>

    @Query(
        """
       SELECT * 
    FROM boxes 
    where office_id IN(
        SELECT office_id 
        FROM offices as o 
        WHERE o.is_online=0 AND o.is_visited=1 )
    """
    )
    suspend fun getOfflineBoxes(): List<LocalBoxEntity>

    @Query("UPDATE boxes SET delivered_at=:time,fake_delivered_at =:fakeDeliveredAt,fake_office_id=:fakeOfficeId WHERE box_id=:boxId")
    suspend fun setBoxDelivery(boxId: String, time: String,fakeOfficeId: Int?,fakeDeliveredAt:String?)

    @Transaction
    suspend fun unloadBoxInOffice(box: LocalBoxEntity) {
        setBoxDelivery(box.boxId, box.deliveredAt,null,null)
        updateOfficeDeliveredBoxAfterUnload(box.officeId)
    }

    @Transaction
    suspend fun takeBoxBack(box: LocalBoxEntity) {
        clearDelivery(box.boxId)
        updateOfficeDeliveredBoxAfterUnload(box.officeId)
    }

    @Query("SELECT * FROM boxes WHERE office_id=:officeId AND delivered_at=''")
    suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity>
}
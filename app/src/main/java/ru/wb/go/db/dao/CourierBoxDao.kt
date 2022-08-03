package ru.wb.go.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.db.entity.courierlocal.LocalLoadingBoxEntity
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

    @Query("UPDATE boxes SET fake_delivered_at=:loadingAt AND fake_office_id=:fakeOfficeId WHERE box_id=:boxId")
    suspend fun setFailedBoxes(fakeOfficeId: String, loadingAt: String,boxId:String)

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

    @Query("UPDATE boxes SET delivered_at=:time WHERE box_id=:boxId")
    suspend fun setBoxDelivery(boxId: String, time: String)

    @Transaction
    suspend fun unloadBoxInOffice(box: LocalBoxEntity) {
        setBoxDelivery(box.boxId, box.deliveredAt)
        updateOfficeDeliveredBoxAfterUnload(box.officeId)
    }

    @Query("UPDATE boxes SET delivered_at='' WHERE box_id=:boxId ")
    fun clearDelivery(boxId: String)

    @Transaction
    suspend fun takeBoxBack(box: LocalBoxEntity) {
        clearDelivery(box.boxId)
        updateOfficeDeliveredBoxAfterUnload(box.officeId)
    }

    @Query("SELECT * FROM boxes WHERE office_id=:officeId AND delivered_at=''")
    suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity>
}
package ru.wb.go.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity

@Dao
interface CourierBoxDao {

    @Query("SELECT * FROM boxes")
    fun readAllBoxesSync(): Single<List<LocalBoxEntity>>

    @Insert
    fun addBox(box: LocalBoxEntity)

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
    fun updateOfficeCountersAfterLoadingBox(officeId: Int)

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
    fun updateOfficeDeliveredBoxAfterUnload(officeId: Int)

    @Transaction
    fun addNewBox(box: LocalBoxEntity) {
        addBox(box)
        updateOfficeCountersAfterLoadingBox(box.officeId)
    }

    @Query("UPDATE boxes SET loading_at=:loadingAt WHERE box_id=:boxId")
    fun updateBoxLoadingAt(boxId: String, loadingAt: String)

    @Insert
    fun addBoxes(boxes: List<LocalBoxEntity>)

    @Query("DELETE FROM boxes")
    fun deleteBoxes()

    @Query("SELECT * FROM boxes")
    fun getBoxes(): List<LocalBoxEntity>

    @Query("SELECT * FROM boxes")
    fun getBoxesLive(): Flowable<List<LocalBoxEntity>>

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
    fun getOfflineBoxes(): List<LocalBoxEntity>

    @Query("UPDATE boxes SET delivered_at=:time WHERE box_id=:boxId")
    fun setBoxDelivery(boxId: String, time: String)

    @Transaction
    fun unloadBoxInOffice(box: LocalBoxEntity) {
        setBoxDelivery(box.boxId, box.deliveredAt)
        updateOfficeDeliveredBoxAfterUnload(box.officeId)
    }

    @Query("UPDATE boxes SET delivered_at='' WHERE box_id=:boxId ")
    fun clearDelivery(boxId:String)

    @Transaction
    fun takeBoxBack(box: LocalBoxEntity) {
        clearDelivery(box.boxId)
        updateOfficeDeliveredBoxAfterUnload(box.officeId)
    }

    @Query("SELECT * FROM boxes WHERE office_id=:officeId AND delivered_at=''")
    fun getRemainBoxes(officeId: Int): Maybe<List<LocalBoxEntity>>
}
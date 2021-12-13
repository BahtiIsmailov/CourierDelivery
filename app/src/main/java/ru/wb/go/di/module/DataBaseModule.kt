package ru.wb.go.di.module

import android.app.Application
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.wb.go.app.DATABASE_NAME
import ru.wb.go.db.AppDatabase
import ru.wb.go.db.dao.*

val databaseModule = module {

    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    fun provideCourierWarehouseDao(database: AppDatabase): CourierWarehouseDao {
        return database.courierWarehouseDao
    }

    fun provideCourierOrderDao(database: AppDatabase): CourierOrderDao {
        return database.courierOrderDao
    }

    fun provideCourierBoxDao(database: AppDatabase): CourierBoxDao {
        return database.courierBoxDao
    }

//    fun provideCourierAccountDao(database: AppDatabase): CourierAccountDao {
//        return database.courierAccountDao
//    }

    fun provideFlightDao(database: AppDatabase): FlightDao {
        return database.flightDao
    }

    fun provideFlightMatchingDao(database: AppDatabase): FlightBoxDao {
        return database.flightMatchingDao
    }

    fun provideWarehouseMatchingBoxDao(database: AppDatabase): WarehouseMatchingBoxDao {
        return database.warehouseMatchingBoxDao
    }

    fun providePvzMatchingBoxDao(database: AppDatabase): PvzMatchingBoxDao {
        return database.pvzMatchingBoxDao
    }

    fun provideDeliveryErrorBoxDao(database: AppDatabase): DeliveryErrorBoxDao {
        return database.deliveryErrorBoxDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideCourierWarehouseDao(get()) }
    single { provideCourierOrderDao(get()) }
    single { provideCourierBoxDao(get()) }
//    single { provideCourierAccountDao(get()) }
    single { provideFlightDao(get()) }
    single { provideFlightMatchingDao(get()) }
    single { provideWarehouseMatchingBoxDao(get()) }
    single { providePvzMatchingBoxDao(get()) }
    single { provideDeliveryErrorBoxDao(get()) }

}
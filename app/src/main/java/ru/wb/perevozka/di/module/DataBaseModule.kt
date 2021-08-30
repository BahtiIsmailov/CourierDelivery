package ru.wb.perevozka.di.module

import android.app.Application
import androidx.room.Room
import ru.wb.perevozka.app.DATABASE_NAME
import ru.wb.perevozka.db.AppDatabase
import ru.wb.perevozka.db.dao.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {

    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    fun provideCourierOrderDao(database: AppDatabase): CourierOrderDao {
        return database.courierOrderDao
    }

    fun provideCourierBoxDao(database: AppDatabase): CourierBoxDao {
        return database.courierBoxDao
    }

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
    single { provideFlightDao(get()) }
    single { provideCourierOrderDao(get()) }
    single { provideCourierBoxDao(get()) }
    single { provideFlightMatchingDao(get()) }
    single { provideWarehouseMatchingBoxDao(get()) }
    single { providePvzMatchingBoxDao(get()) }
    single { provideDeliveryErrorBoxDao(get()) }

}
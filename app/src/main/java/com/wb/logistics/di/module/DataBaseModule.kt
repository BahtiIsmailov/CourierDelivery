package com.wb.logistics.di.module

import android.app.Application
import androidx.room.Room
import com.wb.logistics.app.DATABASE_NAME
import com.wb.logistics.db.AppDatabase
import com.wb.logistics.db.dao.*
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {

    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
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
    single { provideFlightMatchingDao(get()) }
    single { provideWarehouseMatchingBoxDao(get()) }
    single { providePvzMatchingBoxDao(get()) }
    single { provideDeliveryErrorBoxDao(get()) }

}
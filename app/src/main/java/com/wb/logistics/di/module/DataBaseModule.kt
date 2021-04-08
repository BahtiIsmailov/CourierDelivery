package com.wb.logistics.di.module

import android.app.Application
import androidx.room.Room
import com.wb.logistics.app.DATABASE_NAME
import com.wb.logistics.db.AppDatabase
import com.wb.logistics.db.BoxDao
import com.wb.logistics.db.FlightDao
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

    fun provideBoxDao(database: AppDatabase): BoxDao {
        return database.boxDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideFlightDao(get()) }
    single { provideBoxDao(get()) }
}
package com.wb.logistics.di.module

import android.app.Application
import androidx.room.Room
import com.wb.logistics.app.DATABASE_NAME
import com.wb.logistics.db.AppDatabase
import com.wb.logistics.ui.delivery.data.DeliveryDao
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {

    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(application, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    fun provideUserDao(database: AppDatabase): DeliveryDao {
        return database.deliveryDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideUserDao(get()) }
}
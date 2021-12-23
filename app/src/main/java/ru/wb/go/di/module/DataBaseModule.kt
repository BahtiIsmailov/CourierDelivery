package ru.wb.go.di.module

import android.app.Application
import androidx.room.Room
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import ru.wb.go.app.DATABASE_NAME
import ru.wb.go.db.AppDatabase
import ru.wb.go.db.dao.CourierAccountDao
import ru.wb.go.db.dao.CourierBoxDao
import ru.wb.go.db.dao.CourierOrderDao
import ru.wb.go.db.dao.CourierWarehouseDao

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

    fun provideCourierAccountDao(database: AppDatabase): CourierAccountDao {
        return database.courierAccountDao
    }

    single { provideDatabase(androidApplication()) }
    single { provideCourierWarehouseDao(get()) }
    single { provideCourierOrderDao(get()) }
    single { provideCourierBoxDao(get()) }
    single { provideCourierAccountDao(get()) }

}
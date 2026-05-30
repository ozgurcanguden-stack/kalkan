package com.kalkan.app.di

import android.content.Context
import androidx.room.Room
import com.kalkan.app.data.local.KalkanDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KalkanDatabase =
        Room.databaseBuilder(context, KalkanDatabase::class.java, "kalkan.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideEmergencyContactDao(database: KalkanDatabase) = database.emergencyContactDao()

    @Provides
    fun provideOfflineInfoDao(database: KalkanDatabase) = database.offlineInfoDao()
}

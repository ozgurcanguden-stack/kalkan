package com.kalkan.app.di

import com.kalkan.app.data.repository.AfadEarthquakeRepository
import com.kalkan.app.data.repository.FirebaseAuthRepository
import com.kalkan.app.data.repository.FirebaseEmergencyRepository
import com.kalkan.app.domain.repository.AuthRepository
import com.kalkan.app.domain.repository.EarthquakeRepository
import com.kalkan.app.domain.repository.EmergencyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(repository: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindEarthquakeRepository(repository: AfadEarthquakeRepository): EarthquakeRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyRepository(repository: FirebaseEmergencyRepository): EmergencyRepository
}

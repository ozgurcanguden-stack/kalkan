package com.kalkan.app.di

import com.kalkan.app.data.announcement.AnnouncementRepository
import com.kalkan.app.data.announcement.FirebaseAnnouncementRepository
import com.kalkan.app.data.auth.AuthRepository
import com.kalkan.app.data.auth.FirebaseAuthRepository
import com.kalkan.app.data.fcm.FcmRepository
import com.kalkan.app.data.fcm.FirebaseFcmRepository
import com.kalkan.app.data.repository.AfadEarthquakeRepository
import com.kalkan.app.data.repository.FirebaseEmergencyRepository
import com.kalkan.app.data.user.FirebaseUserRepository
import com.kalkan.app.data.user.UserRepository
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
    abstract fun bindUserRepository(repository: FirebaseUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindFcmRepository(repository: FirebaseFcmRepository): FcmRepository

    @Binds
    @Singleton
    abstract fun bindEarthquakeRepository(repository: AfadEarthquakeRepository): EarthquakeRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyRepository(repository: FirebaseEmergencyRepository): EmergencyRepository

    @Binds
    @Singleton
    abstract fun bindAnnouncementRepository(repository: FirebaseAnnouncementRepository): AnnouncementRepository
}

package com.zgrcan.kalkan.di

import com.zgrcan.kalkan.data.alert.EmergencyAlertRepository
import com.zgrcan.kalkan.data.alert.FirebaseEmergencyAlertRepository
import com.zgrcan.kalkan.data.announcement.AnnouncementRepository
import com.zgrcan.kalkan.data.announcement.FirebaseAnnouncementRepository
import com.zgrcan.kalkan.data.auth.AuthRepository
import com.zgrcan.kalkan.data.auth.FirebaseAuthRepository
import com.zgrcan.kalkan.data.fcm.FcmRepository
import com.zgrcan.kalkan.data.fcm.FirebaseFcmRepository
import com.zgrcan.kalkan.data.emergencyprofile.EmergencyProfileRepository
import com.zgrcan.kalkan.data.emergencyprofile.FirebaseEmergencyProfileRepository
import com.zgrcan.kalkan.data.contacts.EmergencyContactRepository
import com.zgrcan.kalkan.data.contacts.FirebaseEmergencyContactRepository
import com.zgrcan.kalkan.data.location.FusedLocationRepository
import com.zgrcan.kalkan.data.location.LocationRepository
import com.zgrcan.kalkan.data.safety.FirebaseSafetyStatusRepository
import com.zgrcan.kalkan.data.safety.SafetyStatusRepository
import com.zgrcan.kalkan.data.repository.AfadEarthquakeRepository
import com.zgrcan.kalkan.data.repository.FirebaseEmergencyRepository
import com.zgrcan.kalkan.data.user.FirebaseUserRepository
import com.zgrcan.kalkan.data.user.UserRepository
import com.zgrcan.kalkan.data.family.FamilyRepository
import com.zgrcan.kalkan.data.family.FirebaseFamilyRepository
import com.zgrcan.kalkan.domain.repository.EarthquakeRepository
import com.zgrcan.kalkan.domain.repository.EmergencyRepository
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

    @Binds
    @Singleton
    abstract fun bindSafetyStatusRepository(repository: FirebaseSafetyStatusRepository): SafetyStatusRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(repository: FusedLocationRepository): LocationRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyContactRepository(
        repository: FirebaseEmergencyContactRepository,
    ): EmergencyContactRepository

    @Binds
    @Singleton
    abstract fun bindFamilyRepository(
        repository: FirebaseFamilyRepository,
    ): FamilyRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyProfileRepository(
        repository: FirebaseEmergencyProfileRepository,
    ): EmergencyProfileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        repository: com.zgrcan.kalkan.data.settings.FirebaseSettingsRepository,
    ): com.zgrcan.kalkan.data.settings.SettingsRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyAlertRepository(
        repository: FirebaseEmergencyAlertRepository,
    ): EmergencyAlertRepository
}

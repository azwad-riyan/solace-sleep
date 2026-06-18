package com.solace.sleep.di

import com.solace.sleep.data.local.dao.ProfileDao
import com.solace.sleep.data.local.dao.SleepSessionDao
import com.solace.sleep.data.local.dao.TagDao
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.remote.DriveApiClient
import com.solace.sleep.data.repository.InsightsRepository
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.data.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSleepSessionRepository(
        sessionDao: SleepSessionDao,
        tagDao: TagDao
    ): SleepSessionRepository = SleepSessionRepository(sessionDao, tagDao)

    @Provides
    @Singleton
    fun provideProfileRepository(
        profileDao: ProfileDao,
        tagDao: TagDao,
        preferences: AppPreferences
    ): ProfileRepository = ProfileRepository(profileDao, tagDao, preferences)

    @Provides
    @Singleton
    fun provideInsightsRepository(
        sessionRepository: SleepSessionRepository,
        profileRepository: ProfileRepository
    ): InsightsRepository = InsightsRepository(sessionRepository, profileRepository)

    @Provides
    @Singleton
    fun provideSyncRepository(
        driveApiClient: DriveApiClient,
        profileDao: ProfileDao,
        sessionDao: SleepSessionDao,
        tagDao: TagDao,
        preferences: AppPreferences
    ): SyncRepository = SyncRepository(driveApiClient, profileDao, sessionDao, tagDao, preferences)
}

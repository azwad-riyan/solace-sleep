package com.solace.sleep.di

import android.content.Context
import com.solace.sleep.data.remote.DriveApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideDriveApiClient(@ApplicationContext context: Context): DriveApiClient {
        return DriveApiClient(context)
    }
}

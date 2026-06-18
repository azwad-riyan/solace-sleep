package com.solace.sleep.di

import android.content.Context
import androidx.room.Room
import com.solace.sleep.data.local.SolaceDatabase
import com.solace.sleep.data.local.dao.ProfileDao
import com.solace.sleep.data.local.dao.SleepSessionDao
import com.solace.sleep.data.local.dao.TagDao
import com.solace.sleep.util.Constants
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
    fun provideDatabase(@ApplicationContext context: Context): SolaceDatabase {
        return Room.databaseBuilder(
            context,
            SolaceDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProfileDao(database: SolaceDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideSleepSessionDao(database: SolaceDatabase): SleepSessionDao = database.sleepSessionDao()

    @Provides
    fun provideTagDao(database: SolaceDatabase): TagDao = database.tagDao()
}

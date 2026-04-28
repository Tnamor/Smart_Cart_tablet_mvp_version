package com.smartcart.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.smartcart.data.local.LastSessionDao
import com.smartcart.data.local.SmartCartDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): SmartCartDatabase =
        Room.databaseBuilder(
            appContext,
            SmartCartDatabase::class.java,
            "smartcart.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideLastSessionDao(db: SmartCartDatabase): LastSessionDao = db.lastSessionDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}


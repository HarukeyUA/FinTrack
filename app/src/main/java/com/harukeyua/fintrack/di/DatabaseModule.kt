package com.harukeyua.fintrack.di

import android.content.Context
import com.harukeyua.fintrack.data.FinDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {

    @Singleton
    @Provides
    fun provideFinDatabase(@ApplicationContext context: Context) = FinDatabase.getInstance(context)

    @Provides
    fun provideFinDao(finDatabase: FinDatabase) = finDatabase.finDao()
}
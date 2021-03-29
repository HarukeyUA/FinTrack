package com.harukeyua.fintrack.di

import com.harukeyua.fintrack.api.MonobankService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideMonobankService(): MonobankService = MonobankService.create()
}
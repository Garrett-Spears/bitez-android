package com.garrett.bitez.di

import com.garrett.bitez.data.repository.PostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun providePostRepository(): PostRepository {
        return PostRepository()
    }
}
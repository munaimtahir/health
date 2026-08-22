package com.vexel.passport.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.vexel.passport.core.database.DatabaseProvider
import com.vexel.passport.core.database.HealthDatabase
import com.vexel.passport.core.datastore.PreferencesStore
import com.vexel.passport.core.files.LocalSecureFileStore
import com.vexel.passport.core.files.SecureFileStore
import com.vexel.passport.core.security.KeystorePinMaterialCipher
import com.vexel.passport.core.security.PinMaterialCipher
import com.vexel.passport.core.notifications.ReminderScheduler
import com.vexel.passport.core.notifications.WorkManagerReminderScheduler

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HealthDatabase =
        DatabaseProvider.create(context)

    @Provides
    @Singleton
    fun providePreferencesStore(@ApplicationContext context: Context): PreferencesStore =
        PreferencesStore(context)

    @Provides
    @Singleton
    fun provideSecureFileStore(@ApplicationContext context: Context): SecureFileStore =
        LocalSecureFileStore(context)

    @Provides
    @Singleton
    fun providePinMaterialCipher(): PinMaterialCipher = KeystorePinMaterialCipher()

    @Provides
    @Singleton
    fun provideReminderScheduler(@ApplicationContext context: Context, database: HealthDatabase): ReminderScheduler =
        WorkManagerReminderScheduler(context, database)

    @Provides
    @Singleton
    fun provideHealthRepository(database: HealthDatabase): com.vexel.passport.core.domain.HealthRepository =
        com.vexel.passport.core.data.HealthRepositoryImpl(database)
}

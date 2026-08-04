package pk.vexel.healthpassport.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pk.vexel.healthpassport.core.database.DatabaseProvider
import pk.vexel.healthpassport.core.database.HealthDatabase
import pk.vexel.healthpassport.core.datastore.PreferencesStore
import pk.vexel.healthpassport.core.files.LocalSecureFileStore
import pk.vexel.healthpassport.core.files.SecureFileStore
import pk.vexel.healthpassport.core.security.KeystorePinMaterialCipher
import pk.vexel.healthpassport.core.security.PinMaterialCipher
import pk.vexel.healthpassport.core.notifications.ReminderScheduler
import pk.vexel.healthpassport.core.notifications.WorkManagerReminderScheduler

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
    fun provideReminderScheduler(@ApplicationContext context: Context): ReminderScheduler = WorkManagerReminderScheduler(context)
}

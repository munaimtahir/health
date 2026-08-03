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
}

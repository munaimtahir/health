package pk.vexel.healthpassport.core.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    fun create(context: Context): HealthDatabase =
        Room.databaseBuilder(context, HealthDatabase::class.java, DatabaseConstants.NAME)
            .build()
}

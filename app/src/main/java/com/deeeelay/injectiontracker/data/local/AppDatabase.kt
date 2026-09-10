package com.deeeelay.injectiontracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deeeelay.injectiontracker.domain.SiteIdLrSwap

@Database(
    entities = [InjectionLogEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): InjectionLogDao

    companion object {
        /** v1.1: remap persisted site ids LEFT_* ↔ RIGHT_* (same body spot). */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val cursor = db.query("SELECT id, site FROM injection_logs")
                val updates = ArrayList<Pair<String, String>>()
                while (cursor.moveToNext()) {
                    val id = cursor.getString(0)
                    val site = if (cursor.isNull(1)) null else cursor.getString(1)
                    val swapped = SiteIdLrSwap.swapPersisted(site)
                    if (site != null && swapped != null && swapped != site) {
                        updates.add(id to swapped)
                    }
                }
                cursor.close()
                for ((id, site) in updates) {
                    db.execSQL(
                        "UPDATE injection_logs SET site = ? WHERE id = ?",
                        arrayOf(site, id),
                    )
                }
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "injection-tracker.db")
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { it.openHelper.writableDatabase }
    }
}

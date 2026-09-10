package com.deeeelay.injectiontracker.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deeeelay.injectiontracker.domain.Frequency
import com.deeeelay.injectiontracker.domain.Regimen
import com.deeeelay.injectiontracker.domain.SiteIdLrSwap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracker")

class TrackerPreferences(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    val snapshot: Flow<PrefsSnapshot> = dataStore.data.map { it.toSnapshot() }

    suspend fun current(): PrefsSnapshot = dataStore.data.first().toSnapshot()

    suspend fun saveSetup(regimen: Regimen) {
        dataStore.edit { prefs ->
            prefs[Keys.SETUP_COMPLETE] = true
            prefs[Keys.MEDICINE_NAME] = regimen.medicineName
            prefs[Keys.DOSAGE] = regimen.dosage
            prefs[Keys.FREQUENCY] = regimen.frequency.name
            prefs[Keys.START_AT] = regimen.startDateTime.toEpochMilli()
            prefs[Keys.NEXT_AT] = regimen.nextInjectionAt.toEpochMilli()
            prefs[Keys.DAY_BEFORE] = true
            prefs[Keys.AT_TIME] = true
        }
    }

    suspend fun updateRegimen(regimen: Regimen) {
        dataStore.edit { prefs ->
            prefs[Keys.MEDICINE_NAME] = regimen.medicineName
            prefs[Keys.DOSAGE] = regimen.dosage
            prefs[Keys.FREQUENCY] = regimen.frequency.name
            prefs[Keys.START_AT] = regimen.startDateTime.toEpochMilli()
            prefs[Keys.NEXT_AT] = regimen.nextInjectionAt.toEpochMilli()
        }
    }

    suspend fun updateNextInjectionAt(next: Instant) {
        dataStore.edit { prefs ->
            prefs[Keys.NEXT_AT] = next.toEpochMilli()
        }
    }

    suspend fun setDayBeforeEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.DAY_BEFORE] = enabled }
    }

    suspend fun setAtTimeEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.AT_TIME] = enabled }
    }

    /**
     * One-shot v1.1 Left↔Right rename for any persisted last-site (or other
     * site-id) string. Room log rows are remapped by AppDatabase MIGRATION_1_2.
     */
    suspend fun migrateLeftRightSiteIdsIfNeeded() {
        dataStore.edit { prefs ->
            if (prefs[Keys.LR_SWAP_V11] == true) return@edit
            val skip = setOf(
                Keys.MEDICINE_NAME.name,
                Keys.DOSAGE.name,
                Keys.FREQUENCY.name,
            )
            val snapshot = prefs.asMap()
            snapshot.forEach { (key, value) ->
                if (value is String && key.name !in skip && SiteIdLrSwap.looksLikeSiteId(value)) {
                    val swapped = SiteIdLrSwap.swapPersisted(value)
                    if (swapped != null && swapped != value) {
                        @Suppress("UNCHECKED_CAST")
                        prefs[key as Preferences.Key<String>] = swapped
                    }
                }
            }
            prefs[Keys.LR_SWAP_V11] = true
        }
    }

    private fun Preferences.toSnapshot(): PrefsSnapshot {
        val setup = this[Keys.SETUP_COMPLETE] == true
        val name = this[Keys.MEDICINE_NAME]
        val dosage = this[Keys.DOSAGE]
        val freqRaw = this[Keys.FREQUENCY]
        val start = this[Keys.START_AT]
        val next = this[Keys.NEXT_AT]
        val regimen = if (setup && name != null && dosage != null && freqRaw != null && start != null && next != null) {
            Regimen(
                medicineName = name,
                dosage = dosage,
                frequency = Frequency.valueOf(freqRaw),
                startDateTime = Instant.ofEpochMilli(start),
                nextInjectionAt = Instant.ofEpochMilli(next),
            )
        } else {
            null
        }
        return PrefsSnapshot(
            setupComplete = setup,
            regimen = regimen,
            dayBeforeEnabled = this[Keys.DAY_BEFORE] ?: true,
            atTimeEnabled = this[Keys.AT_TIME] ?: true,
        )
    }

    data class PrefsSnapshot(
        val setupComplete: Boolean,
        val regimen: Regimen?,
        val dayBeforeEnabled: Boolean,
        val atTimeEnabled: Boolean,
    )

    private object Keys {
        val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        val MEDICINE_NAME = stringPreferencesKey("medicine_name")
        val DOSAGE = stringPreferencesKey("dosage")
        val FREQUENCY = stringPreferencesKey("frequency")
        val START_AT = longPreferencesKey("start_at")
        val NEXT_AT = longPreferencesKey("next_at")
        val DAY_BEFORE = booleanPreferencesKey("day_before")
        val AT_TIME = booleanPreferencesKey("at_time")
        val LAST_SITE = stringPreferencesKey("last_site")
        val LR_SWAP_V11 = booleanPreferencesKey("lr_swap_v11")
    }
}

package com.iptvplayer.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "iptv_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        val CONTINUE_DOWNLOADS_OFFLINE = booleanPreferencesKey("continue_downloads_offline")
        val MAX_PARALLEL_DOWNLOADS = intPreferencesKey("max_parallel_downloads")
        val EXTERNAL_PLAYER = booleanPreferencesKey("external_player")
        val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        val ACTIVE_PLAYLIST_ID = longPreferencesKey("active_playlist_id")
        val APP_THEME = stringPreferencesKey("app_theme") // "DARK", "LIGHT", "SYSTEM"
    }

    val offlineModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.OFFLINE_MODE] ?: false
    }

    val wifiOnlyDownloads: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.WIFI_ONLY_DOWNLOADS] ?: true
    }

    val continueDownloadsOffline: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CONTINUE_DOWNLOADS_OFFLINE] ?: false
    }

    val maxParallelDownloads: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MAX_PARALLEL_DOWNLOADS] ?: 3
    }

    val externalPlayerEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EXTERNAL_PLAYER] ?: false
    }

    val termsAccepted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TERMS_ACCEPTED] ?: false
    }

    val activePlaylistId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVE_PLAYLIST_ID]
    }

    val appTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.APP_THEME] ?: "DARK"
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OFFLINE_MODE] = enabled
        }
    }

    suspend fun setWifiOnlyDownloads(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WIFI_ONLY_DOWNLOADS] = enabled
        }
    }

    suspend fun setContinueDownloadsOffline(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTINUE_DOWNLOADS_OFFLINE] = enabled
        }
    }

    suspend fun setMaxParallelDownloads(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_PARALLEL_DOWNLOADS] = count
        }
    }

    suspend fun setExternalPlayer(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXTERNAL_PLAYER] = enabled
        }
    }

    suspend fun setTermsAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TERMS_ACCEPTED] = accepted
        }
    }

    suspend fun setActivePlaylistId(playlistId: Long?) {
        context.dataStore.edit { preferences ->
            if (playlistId != null) {
                preferences[PreferencesKeys.ACTIVE_PLAYLIST_ID] = playlistId
            } else {
                preferences.remove(PreferencesKeys.ACTIVE_PLAYLIST_ID)
            }
        }
    }

    suspend fun setAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme
        }
    }
}

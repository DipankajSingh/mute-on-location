package com.dipdev.muteonlocation

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: androidx.datastore.core.DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object PreferenceKeys {
    val AUTO_MUTE_KEY= booleanPreferencesKey("on")
    val USER_NAME_KEY = stringPreferencesKey("user_name")
    val USER_AGE_KEY = intPreferencesKey("user_age")
}

class PreferencesManager(private val context: Context) {

    suspend fun saveUserName(userName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USER_NAME_KEY] = userName
        }
    }

    suspend fun saveUserAge(age: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USER_AGE_KEY] = age
        }
    }

    suspend fun autoMuting(autoMute:Boolean){
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.AUTO_MUTE_KEY] = autoMute
        }
    }

    val autoMute: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferenceKeys.AUTO_MUTE_KEY]?:false
        }

    val userName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferenceKeys.USER_NAME_KEY]
        }

    val userAge: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferenceKeys.USER_AGE_KEY]
        }
}

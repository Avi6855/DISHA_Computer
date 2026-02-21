package com.avinashpatil.app.dishacomputer.data.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val DISHA_ID_KEY = stringPreferencesKey("disha_id")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val FULL_NAME_KEY = stringPreferencesKey("full_name")
        private val IS_LOCKED_KEY = booleanPreferencesKey("is_locked")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }
    
    suspend fun saveUserData(userId: String, dishaId: String, email: String, fullName: String) {
        context.userDataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
            preferences[DISHA_ID_KEY] = dishaId
            preferences[EMAIL_KEY] = email
            preferences[FULL_NAME_KEY] = fullName
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }
    
    suspend fun getUserLockStatus(): Boolean {
        return context.userDataStore.data.map { preferences ->
            preferences[IS_LOCKED_KEY] ?: false
        }.first()
    }

    fun getUserLockStatusFlow(): kotlinx.coroutines.flow.Flow<Boolean> {
        return context.userDataStore.data.map { preferences ->
            preferences[IS_LOCKED_KEY] ?: false
        }
    }
    
    suspend fun setUserLockStatus(isLocked: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[IS_LOCKED_KEY] = isLocked
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        return context.userDataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }.first()
    }
    
    suspend fun logout() {
        context.userDataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
            preferences.remove(DISHA_ID_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(FULL_NAME_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
            preferences.remove(IS_LOCKED_KEY)
        }
    }
    
    suspend fun getUserId(): String? {
        return context.userDataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }.first()
    }
    
    suspend fun getDishaId(): String? {
        return context.userDataStore.data.map { preferences ->
            preferences[DISHA_ID_KEY]
        }.first()
    }
}
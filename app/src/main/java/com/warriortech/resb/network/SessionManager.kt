package com.warriortech.resb.network

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.warriortech.resb.model.TblStaff
import androidx.core.content.edit
import com.warriortech.resb.model.GeneralSettings
import com.warriortech.resb.model.RestaurantProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Session manager for handling authentication and user data
 * Uses EncryptedSharedPreferences for secure storage
 */
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SessionManager"
        private const val PREF_NAME = "ResbPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER = "user"
        private const val KEY_COMPANY_CODE = "company_code"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
        private const val KEY_GENERAL_SETTING = "general_setting"
        private const val DECIMAL = "decimal_places"
        private const val KEY_USER_LOGIN = "user_login"
        private const val MAIL_ID="mail_id"
        private const val BLUETOOTH="bluetooth_printer"
    }

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

    /**
     * Initialize the session manager with application context
     * Must be called from Application onCreate or a splash activity
     */
    init {
        try {
            // Create master key for encryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Create encrypted shared preferences
            prefs = EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fall back to regular shared preferences if encryption fails
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }


    /**
     * Save authentication token
     */
    fun saveAuthToken(token: String) {
        checkInitialization()
        prefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun saveUserLogin(userLogin: Boolean) {
        checkInitialization()
        val userLogin = if (userLogin) "true" else "false"
        prefs.edit { putString(KEY_USER_LOGIN, userLogin) }
    }

    fun saveBluetoothPrinter(macAddress: BluetoothDevice) {
        checkInitialization()
        prefs.edit { putString(BLUETOOTH, macAddress.address) }
    }

    fun getBluetoothPrinter(): String? {
        checkInitialization()
        return prefs.getString(BLUETOOTH, null)
    }

    fun clearBluetoothPrinter() {
        checkInitialization()
        prefs.edit { remove(BLUETOOTH) }
    }

    fun getUserLogin(): Boolean {
        checkInitialization()
        val userLogin = prefs.getString(KEY_USER_LOGIN, "false")
        return userLogin == "true"
    }

    /**
     * Get authentication token
     */
    fun getAuthToken(): String? {
        checkInitialization()
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Save user data
     */
    fun saveUser(user: TblStaff) {
        checkInitialization()
        val userJson = gson.toJson(user)
        prefs.edit() { putString(KEY_USER, userJson) }
    }

    /**
     * Get user data
     */
    fun getUser(): TblStaff? {
        checkInitialization()
        val userJson = prefs.getString(KEY_USER, null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, TblStaff::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun saveEmail(mail:String){
        checkInitialization()
        prefs.edit { putString(MAIL_ID,mail) }
    }

    fun getEmail(): String?{
        checkInitialization()
        return prefs.getString(MAIL_ID,"")
    }

    fun saveDecimalPlaces(decimalPlaces: Long) {
        checkInitialization()
        prefs.edit { putLong(DECIMAL, decimalPlaces) }
    }

    fun getDecimalPlaces(): Long {
        checkInitialization()
        return prefs.getLong(DECIMAL, 2)
    }

    fun saveGeneralSetting(setting: GeneralSettings) {
        checkInitialization()
        val settingJson = gson.toJson(setting)
        prefs.edit { putString(KEY_GENERAL_SETTING, settingJson) }
    }

    fun getGeneralSetting(): GeneralSettings? {
        checkInitialization()
        val settingJson = prefs.getString(KEY_GENERAL_SETTING, null)
        return if (settingJson != null) {
            try {
                gson.fromJson(settingJson, GeneralSettings::class.java)
            } catch (e: Exception) {
                null
            }
        } else
            null
    }

    fun saveRestaurantProfile(profile: RestaurantProfile) {
        checkInitialization()
        val profileJson = gson.toJson(profile)
        prefs.edit { putString("restaurant_profile", profileJson) }
    }

    fun getRestaurantProfile(): RestaurantProfile? {
        checkInitialization()
        val profileJson = prefs.getString("restaurant_profile", null)
        return if (profileJson != null) {
            try {
                gson.fromJson(profileJson, RestaurantProfile::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Save company code
     */
    fun saveCompanyCode(companyCode: String) {
        checkInitialization()
        prefs.edit { putString(KEY_COMPANY_CODE, companyCode) }
    }

    /**
     * Get company code
     */
    fun getCompanyCode(): String? {
        checkInitialization()
        return prefs.getString(KEY_COMPANY_CODE, null)
    }

    /**
     * Save subscription end date
     */
    fun saveSubscriptionEndDate(endDate: String) {
        checkInitialization()
        prefs.edit { putString("subscription_end_date", endDate) }
    }

    /**
     * Get subscription end date
     */
    fun getSubscriptionEndDate(): String? {
        checkInitialization()
        return prefs.getString("subscription_end_date", null)
    }

    /**
     * Save last notification date
     */
    fun saveLastNotificationDate(date: String) {
        checkInitialization()
        prefs.edit { putString("last_notification_date", date) }
    }

    /**
     * Get last notification date
     */
    fun getLastNotificationDate(): String? {
        checkInitialization()
        return prefs.getString("last_notification_date", null)
    }

    /**
     * Clear all session data (for logout)
     */
    fun clearSession() {
        checkInitialization()
        prefs.edit { clear() }
    }

    /**
     * Update last sync timestamp
     */
    fun updateLastSyncTimestamp() {
        checkInitialization()
        prefs.edit { putLong(KEY_LAST_SYNC, System.currentTimeMillis()) }
    }

    /**
     * Get last sync timestamp
     */
    fun getLastSyncTimestamp(): Long {
        checkInitialization()
        return prefs.getLong(KEY_LAST_SYNC, 0)
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        checkInitialization()
        return getAuthToken() != null && getUser() != null
    }

    /**
     * Check if session manager is initialized
     */
    private fun checkInitialization() {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("SessionManager not initialized. Call init() first.")
        }
    }
}
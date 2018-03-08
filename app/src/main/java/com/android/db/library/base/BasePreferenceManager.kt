package com.android.db.library.base

import android.annotation.SuppressLint
import android.content.Context

/**
 * Base share preference manager
 *
 * Created by DengBo on 01/03/2018.
 */

open class BasePreferenceManager(protected val context: Context) {

    /**
     * Put preference value
     *
     * @param spName preference name
     * @param key    preference key
     * @param value  preference value
     */
    @SuppressLint("ApplySharedPref")
    protected inline fun <reified T> put(spName: String, key: String, value: T?) {
        val pref = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        val editor = pref.edit()
        when (value) {
            is Int     -> { editor.putInt     (key, value) }
            is Long    -> { editor.putLong    (key, value) }
            is Float   -> { editor.putFloat   (key, value) }
            is String  -> { editor.putString  (key, value) }
            is Boolean -> { editor.putBoolean (key, value) }
            else -> { return }
        }
        editor.commit()
    }

    /**
     * Get preference value except string
     *
     * @param spName   preference name
     * @param key      preference key
     * @param defValue preference default value
     * @return T
     */
    protected inline fun <reified T> get(spName: String, key: String, defValue: T): T {
        val pref = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        return when (defValue) {
            is Int     -> { pref.getInt     (key, defValue) as T }
            is Long    -> { pref.getLong    (key, defValue) as T }
            is Float   -> { pref.getFloat   (key, defValue) as T }
            is Boolean -> { pref.getBoolean (key, defValue) as T }
            else -> { defValue }
        }
    }

    /**
     * Get preference value for string
     *
     * @param spName preference name
     * @param key    preference key
     * @return String
     */
    @Suppress("NOTHING_TO_INLINE")
    protected inline fun get(spName: String, key: String): String? {
        val pref = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        return pref.getString(key, null)
    }

    /**
     * Remove preference key
     *
     * @param spName preference name
     * @param key    preference key
     */
    @SuppressLint("ApplySharedPref")
    @Suppress("NOTHING_TO_INLINE", "PROTECTED_CALL_FROM_PUBLIC_INLINE")
    inline fun remove(spName: String, key: String) {
        val pref = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove(key)
        editor.commit()
    }

}

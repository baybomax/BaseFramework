package com.android.db.library.util

import android.content.Context
import com.android.db.library.base.BasePreferenceManager
import kotlin.reflect.KProperty

/**
 * Data manager
 *
 * Created by DengBo on 01/03/2018.
 */

open class DataManager(context: Context): BasePreferenceManager(context) {

    @Suppress("UNCHECKED_CAST")
    inner class SPDelegate<T>(private val clazz: Class<T>, private val spName: String,
                              private val key: String, private val defValue: T) {

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return when (clazz) {
                Int::class.java     -> { get(spName, key, defValue as Int     ) as T }
                Long::class.java    -> { get(spName, key, defValue as Long    ) as T }
                Float::class.java   -> { get(spName, key, defValue as Float   ) as T }
                Boolean::class.java -> { get(spName, key, defValue as Boolean ) as T }
                else -> { defValue }
            }
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            when (clazz) {
                Int::class.java     -> { put(spName, key, value as Int     ) }
                Long::class.java    -> { put(spName, key, value as Long    ) }
                Float::class.java   -> { put(spName, key, value as Float   ) }
                Boolean::class.java -> { put(spName, key, value as Boolean ) }
                else -> {  }
            }
        }

    }

    inner class SPSDelegate(private val spName: String, private val key: String) {

        operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
            return get(spName, key)
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
            put(spName, key, value)
        }
    }

    /**
     * Preference delegate except string
     *
     * @param spName preference name
     * @param key    preference key
     * @param defValue T
     * @return SPDelegate
     */
    inline fun <reified T> spd(spName: String, key: String, defValue: T): SPDelegate<T> {
        return SPDelegate(T::class.java, spName, key, defValue)
    }

    /**
     * Preference delegate for string
     *
     * @param spName preference name
     * @param key    preference key
     * @return SPSDelegate
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun spd(spName: String, key: String): SPSDelegate {
        return SPSDelegate(spName, key)
    }

}

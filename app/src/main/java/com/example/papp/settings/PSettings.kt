package com.example.papp.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KProperty

@SuppressLint("SdCardPath")
class PSettings(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(this.javaClass.simpleName, Context.MODE_PRIVATE)

    var loggingSize: Int by SharedPrefIntegerProperty(50)
    var connectionType: Int by SharedPrefIntegerProperty(Bluetooth)
    var device: String by SharedPrefStringProperty("")
    var template: String by SharedPrefStringProperty("")
    val templatePath: String by SharedPrefStringProperty("/sdcard/Templates/")


    companion object {
        const val Bluetooth = 0
    }

    /**
     * SharedPrefIntegerProperty
     */
    private class SharedPrefIntegerProperty(private val def: Int) {
        operator fun getValue(sharedPrefHandler: PSettings, property: KProperty<*>): Int {
            return if (sharedPrefHandler.prefs.contains(property.name))
                return sharedPrefHandler.prefs.getInt(property.name, 0)
            else
                def
        }

        operator fun setValue(sharedPrefHandler: PSettings, property: KProperty<*>, i: Int) {
            sharedPrefHandler.prefs.edit().putInt(property.name, i).apply()
        }
    }

    /**
     * SharedPrefStringProperty
     */
    private class SharedPrefStringProperty(private val def: String) {
        operator fun getValue(sharedPrefHandler: PSettings, property: KProperty<*>): String {
            return if (sharedPrefHandler.prefs.contains(property.name))
                sharedPrefHandler.prefs.getString(property.name, def) ?: def
            else
                def
        }

        operator fun setValue(sharedPrefHandler: PSettings, property: KProperty<*>, s: String) {
            sharedPrefHandler.prefs.edit().putString(property.name, s).apply()
        }
    }
}
package com.mahesh.sparrow

import android.app.Application
import com.mahesh.sparrow.data.preferences.DataStoreSparrowPreferences
import com.mahesh.sparrow.data.preferences.SparrowPreferences

class SparrowApplication : Application() {
    val preferences: SparrowPreferences by lazy { DataStoreSparrowPreferences(applicationContext) }
}

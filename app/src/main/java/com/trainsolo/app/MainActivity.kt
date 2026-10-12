package com.trainsolo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.trainsolo.app.store.AppStore
import com.trainsolo.app.ui.App
import com.trainsolo.app.ui.TrainSoloTheme

class MainActivity : ComponentActivity() {
    private lateinit var store: AppStore

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(0xFF0E1526.toInt()),
        )
        super.onCreate(savedInstanceState)
        store = AppStore(applicationContext)
        setContent {
            TrainSoloTheme {
                App(store)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::store.isInitialized) store.refreshToday()
    }
}

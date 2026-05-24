package com.mistweather.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mistweather.tv.ui.screens.MainScreen
import com.mistweather.tv.ui.theme.MistTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MistTheme {
                MainScreen()
            }
        }
    }
}

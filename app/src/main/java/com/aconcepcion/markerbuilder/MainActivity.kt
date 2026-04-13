package com.aconcepcion.markerbuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.aconcepcion.markerbuilder.ui.MarkerBuilderApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarkerBuilderApp()
        }
    }
}

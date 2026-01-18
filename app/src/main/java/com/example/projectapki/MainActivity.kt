package com.example.projectapki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.projectapki.navigation.AppRoot
import com.example.projectapki.ui.theme.ProjectapkiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectapkiTheme {
                AppRoot()
            }
        }
    }
}

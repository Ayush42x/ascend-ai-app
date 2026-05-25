// ─── AscendApp.kt ─────────────────────────────────────────────────────────────
package com.ascendai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AscendApp : Application()


// ─── MainActivity.kt ──────────────────────────────────────────────────────────
package com.ascendai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ascendai.ui.navigation.AscendNavGraph
import com.ascendai.ui.theme.AscendTheme
import com.ascendai.ui.theme.BackgroundDark
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AscendTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = BackgroundDark
                ) {
                    AscendNavGraph()
                }
            }
        }
    }
}

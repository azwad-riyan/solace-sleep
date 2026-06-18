package com.solace.sleep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solace.sleep.ui.navigation.AppNavGraph
import com.solace.sleep.ui.navigation.Route
import com.solace.sleep.ui.theme.SolaceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

            splashScreen.setKeepOnScreenCondition { uiState.isLoading }

            SolaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!uiState.isLoading) {
                        val startDestination: Route = if (uiState.onboardingComplete) {
                            Route.Calendar
                        } else {
                            Route.Onboarding
                        }
                        AppNavGraph(startDestination = startDestination)
                    }
                }
            }
        }
    }
}

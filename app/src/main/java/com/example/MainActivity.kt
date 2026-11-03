package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavHost
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.NUBTKTheme
import com.example.viewmodel.OfficeHoursViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: OfficeHoursViewModel by viewModels {
        OfficeHoursViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NUBTKTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    viewModel.messageEvent.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Crossfade(targetState = showSplash, label = "splash_crossfade") { isSplash ->
                    if (isSplash) {
                        SplashScreen(onFinish = { showSplash = false })
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                        ) { innerPadding ->
                            AppNavHost(
                                viewModel = viewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

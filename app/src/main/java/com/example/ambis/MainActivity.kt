package com.example.ambis

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ambis.home.HomeRoute
import com.example.ambis.home.HomeViewModel
import com.example.ambis.ui.theme.AmbisTheme
import com.example.ambis.util.findActivity

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AmbisTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SecureWindowEffect(showSecure = (uiState as? com.example.ambis.home.HomeUiState.Loaded)?.visible == true)

                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeRoute(
                        state = uiState,
                        onToggleVisible = viewModel::toggleBalanceVisibility,
                        onNavigate = viewModel::onNavigate,
                        onRefresh = { viewModel.refresh(force = true) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SecureWindowEffect(showSecure: Boolean) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    DisposableEffect(showSecure, activity) {
        activity?.window?.let { window ->
            if (showSecure) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        onDispose {
            if (showSecure) {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
}

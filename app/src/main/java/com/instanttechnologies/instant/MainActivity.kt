package com.instanttechnologies.instant

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.instanttechnologies.instant.ui.composable.INSTANTApp
import com.instanttechnologies.instant.ui.composable.INSTANTViewModel
import com.instanttechnologies.instant.ui.theme.INSTANTTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: INSTANTViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = ViewModelProvider(this)[INSTANTViewModel::class.java]
        setContent {
            INSTANTTheme {
                Surface {
                    INSTANTApp(
                        context = this,
                        viewModel = viewModel,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .statusBarsPadding()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("INSTANT", "app in foreground")
        viewModel.goForeground()
    }

    override fun onPause() {
        super.onPause()
        Log.d("INSTANT", "app in background")
        viewModel.goBackground()
    }
}
package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BourseMainLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BourseViewModel

class MainActivity : ComponentActivity() {

    private var sharedViewModel: BourseViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: BourseViewModel = viewModel()
                sharedViewModel = viewModel
                viewModel.initializeServerUrl(applicationContext)
                BourseMainLayout(viewModel = viewModel)
                // Handle deep link if app was opened via baou://payment/...
                handleWaveDeepLink(intent, viewModel)
            }
        }
    }

    // Called when app is already running (singleTop) and receives a new intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sharedViewModel?.let { vm ->
            handleWaveDeepLink(intent, vm)
        }
    }

    private fun handleWaveDeepLink(intent: Intent?, viewModel: BourseViewModel) {
        val data = intent?.data ?: return
        if (data.scheme == "baou" && data.host == "payment") {
            val status = data.getQueryParameter("status") ?: "success"
            val amountParam = data.getQueryParameter("amount")?.toDoubleOrNull()
            if (status == "success" || status == "completed") {
                viewModel.confirmWaveDeposit(amountParam)
            }
        }
    }
}

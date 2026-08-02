package com.example

import android.content.Context
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

    // Indique si l'utilisateur est parti vers Wave (pour détecter le retour)
    private var wentToWave = false

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

    // Appelé quand l'app revient au premier plan
    // (après que l'utilisateur soit revenu de Wave ou d'une autre app)
    override fun onResume() {
        super.onResume()
        sharedViewModel?.let { vm ->
            // Si on a détecté que l'utilisateur est parti vers Wave,
            // on restaure l'état en attente et on redirige vers DEPOSIT
            vm.checkPendingWaveOnResume(applicationContext)
        }
    }

    // Appelé quand l'app passe en arrière-plan (ex: l'utilisateur ouvre Wave)
    override fun onPause() {
        super.onPause()
        // Rien à faire ici, la persistence est déjà dans SharedPreferences
    }

    // Called when app is already running (singleTop) and receives a new intent
    // Déclenché si Wave redirige vers baou://payment/success (deep link automatique)
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
                // Confirmation automatique via deep link Wave
                viewModel.confirmWaveDeposit(amountParam)
            }
        }
    }
}

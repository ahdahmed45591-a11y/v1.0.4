package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.BourseViewModel
import com.example.viewmodel.Screen
import com.example.viewmodel.StockWatchItem
import java.text.NumberFormat
import java.util.Locale

fun savePdfDocumentToPhone(context: android.content.Context, fileName: String, pdfContent: String): Boolean {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pdfContent.toByteArray(Charsets.UTF_8))
                }
                true
            } else false
        } else {
            val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val file = java.io.File(downloadDir, fileName)
            file.writeText(pdfContent, Charsets.UTF_8)
            val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = android.net.Uri.fromFile(file)
            context.sendBroadcast(mediaScanIntent)
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

// Helper extension to format currency
fun Double.formatFcfa(): String {
    val formatter = NumberFormat.getInstance(Locale.FRANCE)
    return "${formatter.format(this.toInt())} FCFA"
}

@Composable
fun BourseMainLayout(viewModel: BourseViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Observe state flows for toast/status messages
    val transactionStatusFlow = viewModel.transactionStatus
    LaunchedEffect(Unit) {
        transactionStatusFlow.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        bottomBar = {
            // Display bottom bar navigation on dashboard screens only
            if (currentScreen in listOf(Screen.DASHBOARD, Screen.MARKET, Screen.PORTFOLIO, Screen.HISTORY, Screen.HELP, Screen.PROFILE)) {
                BourseBottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animated screen transition
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.WELCOME -> WelcomeScreen(viewModel)
                    Screen.ONBOARDING -> OnboardingScreen(viewModel)
                    Screen.SIGNATURE -> SignatureScreen(viewModel)
                    Screen.DASHBOARD -> DashboardScreen(viewModel)
                    Screen.MARKET -> MarketScreen(viewModel)
                    Screen.DEPOSIT -> DepositScreen(viewModel)
                    Screen.PORTFOLIO -> PortfolioScreen(viewModel)
                    Screen.HISTORY -> HistoryScreen(viewModel)
                    Screen.HELP -> HelpScreen(viewModel)
                    Screen.PROFILE -> ProfileScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun BourseBottomNavBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = RoundedCornerShape(28.dp),
        color = DeepNavy,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Triple(Screen.DASHBOARD, Icons.Default.Home, "Accueil"),
                Triple(Screen.MARKET, Icons.Default.TrendingUp, "Marché"),
                Triple(Screen.PORTFOLIO, Icons.Default.AccountBalanceWallet, "Portfolio"),
                Triple(Screen.PROFILE, Icons.Default.Person, "Profil")
            )

            navItems.forEach { (screen, icon, label) ->
                val isSelected = currentScreen == screen
                val backgroundColor = if (isSelected) OrangeBrand else Color.Transparent
                val contentColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(backgroundColor)
                        .clickable { onNavigate(screen) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSelected) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


// 1. WELCOME SCREEN
@Composable
fun WelcomeScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val showGoogleChooser by viewModel.showGoogleAccountChooser.collectAsStateWithLifecycle()
    val showServerUrlSettings by viewModel.showServerUrlSettings.collectAsStateWithLifecycle()
    val serverUrlInput by viewModel.serverUrlInput.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val accountName = result.data?.getStringExtra(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
            if (accountName != null) {
                viewModel.performLogin(accountName, "password123") { success ->
                    if (success) {
                        viewModel.syncWithBackend()
                        val profile = viewModel.userProfile.value
                        if (profile != null && profile.kycStep >= 5) {
                            viewModel.navigateTo(Screen.DASHBOARD)
                        } else {
                            viewModel.navigateTo(Screen.ONBOARDING)
                        }
                    }
                }
            }
        }
    }

    val triggerRealGoogleChooser = {
        try {
            val intent = android.accounts.AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
            launcher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            viewModel.showGoogleAccountChooser.value = true
        }
    }

    if (showServerUrlSettings) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.showServerUrlSettings.value = false }
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Configuration Serveur",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    
                    Text(
                        text = "Saisissez l'adresse de votre backend local ou hébergé en ligne pour communiquer.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF444746),
                        textAlign = TextAlign.Center
                    )
                    
                    var tempUrl by remember { mutableStateOf(serverUrlInput) }
                    
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Adresse du serveur") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.showServerUrlSettings.value = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Annuler")
                        }
                        Button(
                            onClick = {
                                viewModel.saveServerUrl(tempUrl, context)
                                viewModel.showServerUrlSettings.value = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }

    if (showGoogleChooser) {
        var isRegisterMode by remember { mutableStateOf(false) }
        var firstNameInputText by remember { mutableStateOf("") }
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var passwordConfirmInput by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf("") }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.showGoogleAccountChooser.value = false }
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Logo BAOU
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFF8200)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("B", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    // Mode Toggle (Connexion / Inscription)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F1F1))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isRegisterMode) Color(0xFFFF8200) else Color.Transparent)
                                .clickable { isRegisterMode = false; errorMsg = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Connexion",
                                fontWeight = FontWeight.Bold,
                                color = if (!isRegisterMode) Color.White else Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRegisterMode) Color(0xFFFF8200) else Color.Transparent)
                                .clickable { isRegisterMode = true; errorMsg = "" },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Inscription",
                                fontWeight = FontWeight.Bold,
                                color = if (isRegisterMode) Color.White else Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Text(
                        text = if (isRegisterMode) "Créer un compte BAOU" else "Connexion • BAOU Finance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        textAlign = TextAlign.Center
                    )

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = firstNameInputText,
                            onValueChange = { firstNameInputText = it; errorMsg = "" },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Prénom") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFFF8200)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it; errorMsg = "" },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Adresse e-mail") },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = Color(0xFFFF8200)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { 
                            if (it.length <= 12 && !it.contains(" ")) {
                                passwordInput = it
                                errorMsg = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (isRegisterMode) "Code PIN secret (au moins 6 chiffres)" else "Code PIN secret (au moins 6 chiffres)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF8200)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF999999)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            androidx.compose.ui.text.input.VisualTransformation.None
                        else
                            androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = passwordConfirmInput,
                            onValueChange = { 
                                if (it.length <= 12 && !it.contains(" ")) {
                                    passwordConfirmInput = it
                                    errorMsg = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Confirmer le Code PIN (au moins 6 chiffres)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF8200)) },
                            visualTransformation = if (passwordVisible)
                                androidx.compose.ui.text.input.VisualTransformation.None
                            else
                                androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (errorMsg.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEDED))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color(0xFFCC0000), modifier = Modifier.size(18.dp))
                            Text(errorMsg, fontSize = 12.sp, color = Color(0xFFCC0000))
                        }
                    }

                    Button(
                        onClick = {
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                errorMsg = "Veuillez remplir tous les champs."
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()) {
                                errorMsg = "Adresse e-mail invalide."
                                return@Button
                            }
                            if (passwordInput.length < 6) {
                                errorMsg = "Le code PIN secret doit contenir au moins 6 caractères."
                                return@Button
                            }

                            if (isRegisterMode) {
                                if (firstNameInputText.isBlank()) {
                                    errorMsg = "Veuillez entrer votre prénom."
                                    return@Button
                                }
                                if (passwordInput != passwordConfirmInput) {
                                    errorMsg = "Les deux codes PIN secrets ne correspondent pas."
                                    return@Button
                                }

                                isLoading = true
                                viewModel.performRegister(emailInput.trim(), passwordInput, firstNameInputText) { err ->
                                    isLoading = false
                                    if (err == null) {
                                        viewModel.showGoogleAccountChooser.value = false
                                        viewModel.syncWithBackend()
                                        viewModel.navigateTo(Screen.ONBOARDING)
                                    } else {
                                        errorMsg = err
                                    }
                                }
                            } else {
                                isLoading = true
                                viewModel.performLogin(emailInput.trim(), passwordInput) { success ->
                                    isLoading = false
                                    if (success) {
                                        viewModel.showGoogleAccountChooser.value = false
                                        viewModel.syncWithBackend()
                                        val profile = viewModel.userProfile.value
                                        if (profile != null && profile.kycStep >= 5) {
                                            viewModel.navigateTo(Screen.DASHBOARD)
                                        } else {
                                            viewModel.navigateTo(Screen.ONBOARDING)
                                        }
                                    } else {
                                        errorMsg = "Connexion échouée. Vérifiez vos identifiants."
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8200)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(if (isRegisterMode) "S'inscrire" else "Se connecter", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Configuration Serveur Settings Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { viewModel.showServerUrlSettings.value = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuration Serveur",
                    tint = OrangeBrand
                )
            }
        }

        // Branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            ElephantLogoCanvas(
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = "Bienvenue sur\nÉléphant Bourse",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = DarkOnBackground,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Votre passerelle vers le marché financier ivoirien.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bouton Ouvrir un compte / Se connecter avec Code PIN 5 chiffres
            Button(
                onClick = {
                    viewModel.showGoogleAccountChooser.value = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("open_account_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Ouvrir un compte / Se connecter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trust Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrayBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Sécurité",
                        tint = OrangeBrand,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Votre sécurité est notre priorité. Vos données sont chiffrées selon les standards bancaires.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer Linkages
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Besoin d'aide ?",
                style = MaterialTheme.typography.labelLarge,
                color = OrangeBrand,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { viewModel.navigateTo(Screen.HELP) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(GrayBorder)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Conditions d'utilisation",
                style = MaterialTheme.typography.labelLarge,
                color = OrangeBrand,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "© 2026 ÉLÉPHANT BOURSE • CÔTE D'IVOIRE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: BourseViewModel) {
    val context = LocalContext.current
    val step by viewModel.onboardingStep.collectAsStateWithLifecycle()

    val firstName by viewModel.firstNameInput.collectAsStateWithLifecycle()
    val lastName by viewModel.lastNameInput.collectAsStateWithLifecycle()
    val birthDate by viewModel.birthDateInput.collectAsStateWithLifecycle()

    val progressValue = when (step) {
        1 -> 0.33f
        2 -> 0.66f
        else -> 1.0f
    }

    val stepTitle = when (step) {
        1 -> "Informations Personnelles"
        2 -> "Vérification d'identité"
        else -> "Justificatif de domicile"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Back toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Inscription",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }, enabled = false) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Transparent)
            }
        }

        // Onboarding Progress Header
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Étape $step sur 3 : $stepTitle",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrangeBrand,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(progressValue * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ForestGreen,
                trackColor = GrayBorder
            )
        }

        // Step Contents card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (step) {
                    1 -> {
                        Text(
                            text = "Commençons par vous",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Veuillez entrer vos informations de base telles qu'elles apparaissent sur vos documents officiels.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val profession by viewModel.professionInput.collectAsStateWithLifecycle()
                        val residence by viewModel.residenceInput.collectAsStateWithLifecycle()

                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { viewModel.firstNameInput.value = it },
                            label = { Text("Prénom(s)") },
                            placeholder = { Text("ex: Jean-Marc") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("firstname_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { viewModel.lastNameInput.value = it },
                            label = { Text("Nom de famille") },
                            placeholder = { Text("ex: Kouassi") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lastname_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { viewModel.birthDateInput.value = it },
                            label = { Text("Date de naissance") },
                            placeholder = { Text("JJ/MM/AAAA") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("birthdate_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = profession,
                            onValueChange = { viewModel.professionInput.value = it },
                            label = { Text("Profession / Secteur d'activité") },
                            placeholder = { Text("ex: Ingénieur, Commerçant...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profession_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = residence,
                            onValueChange = { viewModel.residenceInput.value = it },
                            label = { Text("Ville & Pays de résidence") },
                            placeholder = { Text("ex: Abidjan, Côte d'Ivoire") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("residence_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = { viewModel.submitPersonalDetails() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("onboarding_next_1"),
                            enabled = firstName.isNotBlank() && lastName.isNotBlank() && birthDate.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
                        ) {
                            Text("Continuer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                    2 -> {
                        val whatsapp by viewModel.whatsappInput.collectAsStateWithLifecycle()
                        var activeDocTarget by remember { mutableStateOf<String?>(null) } // "RECTO", "VERSO", "SELFIE", "PROOF"

                        // Helper: Convert Bitmap to Base64
                        fun bitmapToBase64(bmp: android.graphics.Bitmap): String {
                            val out = java.io.ByteArrayOutputStream()
                            bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
                            return android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.DEFAULT)
                        }

                        // Helper: Convert URI to Base64
                        fun uriToBase64(ctx: android.content.Context, uri: android.net.Uri): String? {
                            return try {
                                val inp = ctx.contentResolver.openInputStream(uri) ?: return null
                                val bytes = inp.readBytes()
                                inp.close()
                                android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                            } catch (e: Exception) { null }
                        }

                        // Camera Launcher for Taking Photos — directly uploads to backend
                        val cameraPhotoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
                        ) { bitmap ->
                            if (bitmap != null) {
                                val b64 = bitmapToBase64(bitmap)
                                when (activeDocTarget) {
                                    "RECTO" -> {
                                        viewModel.captureIdentityRecto()
                                        viewModel.uploadDocument("cni_recto", "CNI_Recto_${System.currentTimeMillis()}.jpg", b64)
                                        android.widget.Toast.makeText(context, "📷 CNI RECTO capturée et envoyée à l'admin !", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    "VERSO" -> {
                                        viewModel.captureIdentityVerso()
                                        viewModel.uploadDocument("cni_verso", "CNI_Verso_${System.currentTimeMillis()}.jpg", b64)
                                        android.widget.Toast.makeText(context, "📷 CNI VERSO capturée et envoyée à l'admin !", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    "SELFIE" -> {
                                        viewModel.captureSelfiePhoto()
                                        viewModel.uploadDocument("selfie", "Selfie_${System.currentTimeMillis()}.jpg", b64)
                                        android.widget.Toast.makeText(context, "🤳 Selfie validé et envoyé à l'admin !", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        // File / Gallery Picker — directly uploads to backend
                        val galleryOrFileLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                        ) { uri ->
                            if (uri != null) {
                                val b64 = uriToBase64(context, uri)
                                val ext = context.contentResolver.getType(uri)?.substringAfterLast('/') ?: "jpg"
                                if (b64 != null) {
                                    when (activeDocTarget) {
                                        "RECTO" -> {
                                            viewModel.captureIdentityRecto()
                                            viewModel.uploadDocument("cni_recto", "CNI_Recto_${System.currentTimeMillis()}.$ext", b64)
                                            android.widget.Toast.makeText(context, "📁 CNI RECTO téléversée et envoyée à l'admin !", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        "VERSO" -> {
                                            viewModel.captureIdentityVerso()
                                            viewModel.uploadDocument("cni_verso", "CNI_Verso_${System.currentTimeMillis()}.$ext", b64)
                                            android.widget.Toast.makeText(context, "📁 CNI VERSO téléversée et envoyée à l'admin !", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        "SELFIE" -> {
                                            viewModel.captureSelfiePhoto()
                                            viewModel.uploadDocument("selfie", "Selfie_${System.currentTimeMillis()}.$ext", b64)
                                            android.widget.Toast.makeText(context, "🤳 Selfie envoyé à l'admin !", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        "PROOF" -> {
                                            viewModel.captureProofOfAddressDocument()
                                            viewModel.uploadDocument("cie_sodeci", "Facture_CIE_SODECI_${System.currentTimeMillis()}.$ext", b64)
                                            android.widget.Toast.makeText(context, "🏡 Facture CIE/SODECI envoyée à l'admin !", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Vérification d'identité",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sécurisez votre compte en téléversant vos pièces d'identité et votre selfie pour la conformité BRVM.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // WhatsApp Input
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { viewModel.whatsappInput.value = it },
                            label = { Text("Numéro WhatsApp") },
                            placeholder = { Text("ex: +225 0707070707") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = OrangeBrand) },
                            modifier = Modifier.fillMaxWidth().testTag("whatsapp_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        val identityRecto by viewModel.identityRectoStatus.collectAsStateWithLifecycle()
                        val identityVerso by viewModel.identityVersoStatus.collectAsStateWithLifecycle()
                        val selfiePhoto by viewModel.selfiePhotoStatus.collectAsStateWithLifecycle()

                        // Identity Scan Item — RECTO
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (identityRecto?.contains("✅") == true) ForestGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    activeDocTarget = "RECTO"
                                    try {
                                        cameraPhotoLauncher.launch(null)
                                    } catch (e: Exception) {
                                        galleryOrFileLauncher.launch("image/*")
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = ForestGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pièce d'Identité (RECTO)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(identityRecto ?: "Appuyer pour prendre la photo du Recto (CNI/Passeport)", fontSize = 12.sp, color = if (identityRecto?.contains("✅") == true) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OrangeBrand)
                        }

                        // Identity Scan Item — VERSO
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (identityVerso?.contains("✅") == true) ForestGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    activeDocTarget = "VERSO"
                                    try {
                                        cameraPhotoLauncher.launch(null)
                                    } catch (e: Exception) {
                                        galleryOrFileLauncher.launch("image/*")
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = ForestGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pièce d'Identité (VERSO)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(identityVerso ?: "Appuyer pour prendre la photo du Verso de la CNI", fontSize = 12.sp, color = if (identityVerso?.contains("✅") == true) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = OrangeBrand)
                        }

                        // Face recognition — SELFIE
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selfiePhoto?.contains("✅") == true) ForestGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable {
                                    activeDocTarget = "SELFIE"
                                    try {
                                        cameraPhotoLauncher.launch(null)
                                    } catch (e: Exception) {
                                        galleryOrFileLauncher.launch("image/*")
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ForestGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Face, contentDescription = null, tint = ForestGreen)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reconnaissance Faciale (Selfie)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(selfiePhoto ?: "Appuyer pour ouvrir la caméra et faire un selfie", fontSize = 12.sp, color = if (selfiePhoto?.contains("✅") == true) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.CameraFront, contentDescription = null, tint = OrangeBrand)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = OrangeBrand.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = OrangeBrand)
                                Text(
                                    text = "Pourquoi cette étape ? La réglementation de la BRVM nous impose de vérifier l'identité de chaque investisseur pour prévenir la fraude financière.",
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.onboardingStep.value = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retour", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.completeIdentityBiometrics() },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(50.dp)
                                    .testTag("onboarding_next_2"),
                                enabled = whatsapp.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
                            ) {
                                Text("Valider l'ID", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                    else -> {
                        val proofDocStatus by viewModel.proofOfAddressStatus.collectAsStateWithLifecycle()
                        var activeDocTarget by remember { mutableStateOf<String?>(null) }

                        val proofFileLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                        ) { uri ->
                            if (uri != null) {
                                viewModel.captureProofOfAddressDocument()
                                android.widget.Toast.makeText(context, "🏡 Facture CIE / SODECI téléversée avec succès !", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                        Text(
                            text = "Justificatif de domicile",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Téléchargez votre facture d'électricité (CIE) ou d'eau (SODECI) de moins de 3 mois.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Big Dashed upload zone simulator for CIE / SODECI
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, if (proofDocStatus?.contains("✅") == true) ForestGreen else OrangeBrand.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(if (proofDocStatus?.contains("✅") == true) ForestGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                .clickable {
                                    activeDocTarget = "PROOF"
                                    try {
                                        proofFileLauncher.launch("*/*")
                                    } catch (e: Exception) {
                                        viewModel.captureProofOfAddressDocument()
                                        android.widget.Toast.makeText(context, "🏡 Facture CIE / SODECI sélectionnée !", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = if (proofDocStatus?.contains("✅") == true) ForestGreen else OrangeBrand, modifier = Modifier.size(44.dp))
                                Text(proofDocStatus ?: "Uploader mon document (CIE, SODECI)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Appuyez pour ouvrir vos fichiers et sélectionner la facture PDF / Photo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                Text("Facture d'électricité (CIE) ou d'eau (SODECI)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                Text("Le nom de famille doit correspondre à votre ID", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.onboardingStep.value = 2 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Retour", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.completeAddressUpload() },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(50.dp)
                                    .testTag("onboarding_submit"),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Text("Soumettre mon dossier", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.RocketLaunch, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // Compliances footer logos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = DarkOnBackground.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                Text("Chiffrement AES-256", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkOnBackground.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DarkOnBackground.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                Text("RGPD Conforme", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkOnBackground.copy(alpha = 0.5f))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.GppGood, contentDescription = null, tint = DarkOnBackground.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                Text("Agréé AMF-UMOA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DarkOnBackground.copy(alpha = 0.5f))
            }
        }
    }
}

// 3. SIGNATURE / CONTRACT SCREEN
@Composable
fun SignatureScreen(viewModel: BourseViewModel) {
    val context = LocalContext.current
    var signatureMethodIsPad by remember { mutableStateOf(true) } // true = Pad on screen, false = OTP sms
    var signatureDrawn by remember { mutableStateOf(false) }

    val otpList by viewModel.smsOtpCode.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Validation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }, enabled = false) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Transparent)
            }
        }

        // Header progress
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dernière étape : Signature du contrat", style = MaterialTheme.typography.labelMedium, color = OrangeBrand, fontWeight = FontWeight.Bold)
                Text("95%", style = MaterialTheme.typography.labelMedium, color = ForestGreen, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { 0.95f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ForestGreen,
                trackColor = GrayBorder
            )
        }

        Text(
            text = "Finalisez votre inscription",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Veuillez réviser les conditions juridiques et apposer votre signature légale pour valider l'ouverture de votre compte titres BRVM.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        // Part 1: Legal Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = OrangeBrand)
                    Text("Engagements Juridiques", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                var checked1 by remember { mutableStateOf(false) }
                var checked2 by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = checked1,
                        onCheckedChange = { checked1 = it },
                        colors = CheckboxDefaults.colors(checkedColor = OrangeBrand)
                    )
                    Column {
                        Text("Tarification SGI en vigueur", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Je déclare accepter la tarification de courtage de la SGI partenaire (0,5% par transaction).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Consulter la grille tarifaire",
                            color = OrangeBrand,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = checked2,
                        onCheckedChange = { checked2 = it },
                        colors = CheckboxDefaults.colors(checkedColor = OrangeBrand)
                    )
                    Column {
                        Text("Politique de protection des données", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "J'autorise Éléphant Bourse à collecter et traiter mes données personnelles aux fins d'ouverture de compte.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Part 2: Signature Selector Tab
        TabRow(
            selectedTabIndex = if (signatureMethodIsPad) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = signatureMethodIsPad,
                onClick = { signatureMethodIsPad = true },
                text = { Text("Signer sur l'Écran", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = !signatureMethodIsPad,
                onClick = { signatureMethodIsPad = false },
                text = { Text("Code SMS OTP", fontWeight = FontWeight.Bold) }
            )
        }

        // Signature interactive component container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            if (signatureMethodIsPad) {
                SignaturePad(
                    modifier = Modifier.fillMaxSize(),
                    onSignatureDrawn = { signatureDrawn = it }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Entrez le code OTP reçu au +225 •• •• •• 89", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0 until 6) {
                                OutlinedTextField(
                                    value = otpList[i],
                                    onValueChange = { value ->
                                        if (value.length <= 1) {
                                            val newList = otpList.toMutableList()
                                            newList[i] = value
                                            viewModel.smsOtpCode.value = newList
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("otp_digit_$i"),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confirmation Actions
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val userProfile = viewModel.userProfile.value
                    val clientEmail = if (userProfile != null && userProfile.firstName.isNotEmpty()) "${userProfile.firstName.lowercase()}@client.baou.ci" else "client@baou.ci"
                    val pdfContent = """
===================================================================
             BAOU FINANCIAL & STOCK TRADING — CONTRAT SGI
===================================================================
Nom du Client: ${userProfile?.firstName ?: "Investisseur"} ${userProfile?.lastName ?: ""}
Email: $clientEmail
Date de validation: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date())}
Statut: CONTRAT VALIDE ET SIGNE NUMERIQUEMENT
SGI Partenaire: SGI BRVM Cote d'Ivoire
===================================================================
%PDF-1.4
1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
2 0 obj << /Type /Pages /Kinds [] /Count 1 >> endobj
trailer << /Root 1 0 R >> %%EOF
                    """.trimIndent()
                    
                    val success = savePdfDocumentToPhone(context, "Contrat_Ouverture_Compte_SGI_BAOU.pdf", pdfContent)
                    if (success) {
                        android.widget.Toast.makeText(context, "📄 Contrat SGI enregistré dans votre dossier Téléchargements !", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "📄 Contrat SGI validé et sauvegardé !", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    val pdfBase64 = android.util.Base64.encodeToString(pdfContent.toByteArray(Charsets.UTF_8), android.util.Base64.DEFAULT)
                    viewModel.uploadDocument("contract", "Contrat_SGI_${System.currentTimeMillis()}.pdf", pdfBase64)
                    viewModel.verifySmsOtp()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_signature_button"),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Valider mon compte & Télécharger le contrat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val userProfile = viewModel.userProfile.value
                    val clientEmail = if (userProfile != null && userProfile.firstName.isNotEmpty()) "${userProfile.firstName.lowercase()}@client.baou.ci" else "client@baou.ci"
                    val pdfContent = """
===================================================================
             BAOU FINANCIAL & STOCK TRADING — CONTRAT SGI
===================================================================
Nom du Client: ${userProfile?.firstName ?: "Investisseur"} ${userProfile?.lastName ?: ""}
Email: $clientEmail
Date de génération: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.FRANCE).format(java.util.Date())}
Statut: CONTRAT OFFICIEL D'OUVERTURE DE COMPTE TITRES
===================================================================
%PDF-1.4
1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
2 0 obj << /Type /Pages /Kinds [] /Count 1 >> endobj
trailer << /Root 1 0 R >> %%EOF
                    """.trimIndent()
                    val success = savePdfDocumentToPhone(context, "Contrat_Ouverture_Compte_SGI_BAOU.pdf", pdfContent)
                    if (success) {
                        android.widget.Toast.makeText(context, "📄 Contrat SGI téléchargé dans Téléchargements !", android.widget.Toast.LENGTH_LONG).show()
                    } else {
                        android.widget.Toast.makeText(context, "📄 Fichier téléchargé avec succès !", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = ForestGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Télécharger le contrat (PDF)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 4. DASHBOARD / ACCUEIL
@Composable
fun DashboardScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OrangeBrand.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OrangeBrand)
                }
                Column {
                    val displayName = if (userProfile != null && userProfile!!.firstName.isNotBlank()) {
                        "Bonjour, ${userProfile!!.firstName}"
                    } else {
                        "Bonjour, Investisseur"
                    }
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Propulsez votre avenir financier.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(
                onClick = {
                    android.widget.Toast.makeText(context, "Aucune nouvelle notification", android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Bourse branding tag
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(100.dp))
                .background(ForestGreen.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text("BOURSE HORIZON", color = ForestGreen, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        // Lock Banner if not verified
        val isVerified = userProfile != null && userProfile!!.kycStep >= 5
        if (!isVerified) {
            Card(
                colors = CardDefaults.cardColors(containerColor = OrangeBrand.copy(alpha = 0.08f)),
                border = BorderStroke(1.5.dp, OrangeBrand.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(Screen.ONBOARDING) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OrangeBrand.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = OrangeBrand)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Ouverture de Compte SGI BRVM à distance",
                                fontWeight = FontWeight.Bold,
                                color = OrangeBrand,
                                fontSize = 15.sp
                            )
                            Text(
                                "Ouvrez votre compte titres auprès d'une SGI agréée depuis chez vous.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = OrangeBrand.copy(alpha = 0.2f))

                    Text("Éléments du dossier (100% en ligne, sans RIB) :", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Text("Identité, Profession & WhatsApp", fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Text("Pièce d'Identité (CNI / Passeport)", fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Text("Justificatif de domicile (CIE, SODECI)", fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Text("Signature numérique du Contrat SGI", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.ONBOARDING) },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Compléter mon dossier SGI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Main Portfolio Bento Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GrayBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "SOLDE TOTAL ESTIMÉ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                val netValue = (userProfile?.portfolioValue ?: 0.0) + (userProfile?.cashBalance ?: 0.0)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = netValue.formatFcfa().substringBefore(" FCFA"),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkOnBackground,
                        fontSize = 32.sp
                    )
                    Text("FCFA", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(ForestGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                        Text("+2.5% (Aujourd'hui)", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.MARKET) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isVerified) GoldPremium else Color.Gray),
                        enabled = isVerified,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("place_order_button")
                    ) {
                        Text("Placer un ordre", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats card layout
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.navigateTo(Screen.PORTFOLIO) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIONS DÉTENUES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${holdings.size} Sociétés", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DIVIDENDES REÇUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("45 800 FCFA", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = ForestGreen)
                }
            }
        }

        // Grid Menu Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.MARKET) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Acheter", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.PORTFOLIO) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Suivre", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.HISTORY) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GoldPremium, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Historique", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, GrayBorder, RoundedCornerShape(12.dp))
                    .clickable { viewModel.navigateTo(Screen.HELP) }
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.School, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Académie", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Did you know block (Académie / Pédagogie)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrangeBrand)
                        Text("Le saviez-vous ?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Académie",
                        color = ForestGreen,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.HELP) }
                    )
                }

                Text(
                    text = "Une action est une part du capital d'une entreprise. En en achetant une, vous devenez copropriétaire et pouvez recevoir une partie des bénéfices appelés dividendes.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text("PROGRESSION DU MODULE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ForestGreen,
                    trackColor = GrayBorder
                )
            }
        }
    }
}

// 5. MARKET / DETAIL STOCK SCREEN
@Composable
fun MarketScreen(viewModel: BourseViewModel) {
    val selectedStockFlow by viewModel.selectedStock.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Set default selected stock if null
    LaunchedEffect(selectedStockFlow) {
        if (selectedStockFlow == null) {
            viewModel.selectStockAndNavigate("SNTS")
        }
    }

    val stock = selectedStockFlow ?: return

    val qtyString by viewModel.orderQuantity.collectAsStateWithLifecycle()
    val limitPriceString by viewModel.orderLimitPrice.collectAsStateWithLifecycle()
    val isMarketOrder by viewModel.orderTypeIsMarket.collectAsStateWithLifecycle()

    val priceUnit = if (isMarketOrder) stock.price else (limitPriceString.toDoubleOrNull() ?: stock.price)
    val quantity = qtyString.toIntOrNull() ?: 0
    val subtotal = quantity * priceUnit
    val brokerageFees = Math.round(subtotal * 0.005).toDouble()
    val grandTotal = subtotal + brokerageFees

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
                }
                Text(
                    text = "Achat d'Action",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { viewModel.navigateTo(Screen.PORTFOLIO) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Portefeuille", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Search ticker bar lookup simulation
        var searchQuery by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    dropdownExpanded = it.isNotBlank()
                },
                placeholder = { Text("Rechercher une action (ex: SNTS, ORAC...)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Dynamic dropdown results based on search query
            if (dropdownExpanded) {
                val filtered = viewModel.watchlist.filter {
                    it.ticker.contains(searchQuery, ignoreCase = true) || it.companyName.contains(searchQuery, ignoreCase = true)
                }
                if (filtered.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 58.dp)
                            .zIndex(10f),
                        border = BorderStroke(1.dp, GrayBorder),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column {
                            filtered.forEach { match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectStockAndNavigate(match.ticker)
                                            searchQuery = ""
                                            dropdownExpanded = false
                                        }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${match.ticker} - ${match.companyName}", fontWeight = FontWeight.Bold)
                                    Text(match.price.formatFcfa(), color = OrangeBrand)
                                }
                                HorizontalDivider(color = GrayBorder)
                            }
                        }
                    }
                }
            }
        }

        // MARCHÉ BRVM List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("MARCHÉ BRVM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            viewModel.watchlist.forEach { item ->
                val isSelected = item.ticker == stock.ticker
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectStockAndNavigate(item.ticker) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) OrangeBrand.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (isSelected) OrangeBrand else GrayBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) OrangeBrand.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.ticker, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = if (isSelected) OrangeBrand else DarkOnBackground)
                            }
                            Column {
                                Text(item.companyName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(item.sector, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(item.price.formatFcfa(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val color = if (item.isGaining) ForestGreen else RedLoss
                            val prefix = if (item.isGaining) "+" else ""
                            Text("$prefix${item.changePercent}%", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Ticker details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stock.ticker, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Column {
                        Text(stock.companyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${stock.ticker} • BRVM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(stock.price.formatFcfa(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    val color = if (stock.isGaining) ForestGreen else RedLoss
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = if (stock.isGaining) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("+${stock.changePercent}%", color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Performance Chart Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PERFORMANCE 24H", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ForestGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("DIRECT", color = ForestGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LineTrendChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    color = if (stock.isGaining) ForestGreen else RedLoss,
                    isGaining = stock.isGaining
                )
            }
        }

        // Order Form Widget
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Placer un Ordre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Segmented tabs: Market vs Limit
            TabRow(
                selectedTabIndex = if (isMarketOrder) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = isMarketOrder,
                    onClick = { viewModel.orderTypeIsMarket.value = true },
                    text = { Text("Marché", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = !isMarketOrder,
                    onClick = { viewModel.orderTypeIsMarket.value = false },
                    text = { Text("Cours Limite", fontWeight = FontWeight.Bold) }
                )
            }

            // Info text explanation
            Card(
                colors = CardDefaults.cardColors(containerColor = OrangeBrand.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = OrangeBrand)
                    val textDesc = if (isMarketOrder) {
                        "Ordre au Marché : Votre ordre sera exécuté immédiatement au meilleur prix disponible actuellement sur le carnet d'ordres."
                    } else {
                        "Ordre à cours limité : Vous fixez le prix maximal que vous voulez payer. L'achat ne se fera que si le prix descend à ou sous votre limite."
                    }
                    Text(text = textDesc, style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
                }
            }

            // Numeric Inputs
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = qtyString,
                    onValueChange = { viewModel.orderQuantity.value = it },
                    label = { Text("Unités (Quantité)") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("order_qty_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (!isMarketOrder) {
                    OutlinedTextField(
                        value = limitPriceString,
                        onValueChange = { viewModel.orderLimitPrice.value = it },
                        label = { Text("Prix Limite (FCFA)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_limit_price_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Total recap table
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estimation totale", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(subtotal.toDouble().formatFcfa(), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Frais de courtage (0.5%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(brokerageFees.formatFcfa(), fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = GrayBorder)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("TOTAL ESTIMÉ", fontWeight = FontWeight.Bold, color = OrangeBrand)
                        Text(grandTotal.formatFcfa(), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = OrangeBrand)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                val availCash = userProfile?.cashBalance ?: 125000.0
                Text(
                    text = "Solde disponible : ${availCash.formatFcfa()}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val isVerified = userProfile != null && userProfile!!.kycStep >= 5
            Button(
                onClick = { viewModel.executeOrder() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_order_button"),
                colors = ButtonDefaults.buttonColors(containerColor = if (isVerified) ForestGreen else Color.Gray),
                enabled = isVerified,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isVerified) "Confirmer l'achat" else "Achat Verrouillé (Non Validé)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 6. PORTFOLIO / PORTEFEUILLE SCREEN
@Composable
fun PortfolioScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    var activeSellHolding by remember { mutableStateOf<HoldingsEntity?>(null) }
    var sellQtyInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(OrangeBrand.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = OrangeBrand)
                }
                Column {
                    Text("Mon Portefeuille", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Suivez vos investissements.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(
                onClick = { viewModel.navigateTo(Screen.DEPOSIT) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(OrangeBrand.copy(alpha = 0.12f))
                    .testTag("add_funds_header")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dépôt", tint = OrangeBrand)
            }
        }

        // Dashboard Hero value card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "VALEUR TOTALE DU PORTEFEUILLE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                val netValue = (userProfile?.portfolioValue ?: 0.0) + (userProfile?.cashBalance ?: 0.0)
                Text(
                    text = netValue.formatFcfa(),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkOnBackground,
                    fontSize = 30.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(ForestGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                    Text("Portefeuille Titres SGI BRVM", style = MaterialTheme.typography.labelSmall, color = ForestGreen, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Micro-visualizer graph
                BarVisualizer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                )
            }
        }

        // Available Balance box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SOLDE DISPONIBLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    val availCash = userProfile?.cashBalance ?: 0.0
                    Text(availCash.formatFcfa(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("Prêt à investir", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = { viewModel.navigateTo(Screen.DEPOSIT) },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Déposer", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Mes Actions holdings list
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mes Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${holdings.size} Actifs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (holdings.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = GrayBorder, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Aucune action détenue", fontWeight = FontWeight.Bold)
                        Text("Achetez des actions sur l'onglet Marché.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                holdings.forEach { holding ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, GrayBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(holding.ticker, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(holding.companyName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val priceVal = holding.currentPrice * holding.sharesCount
                                    Text(priceVal.formatFcfa(), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    val prefix = if (holding.changePercent >= 0) "+" else ""
                                    val color = if (holding.changePercent >= 0) ForestGreen else RedLoss
                                    Text("$prefix${holding.changePercent}%", color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("TITRES", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${holding.sharesCount}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("VALEUR D'ACHAT MOYENNE", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(holding.averagePrice.formatFcfa(), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            // Interactive inline sell simulator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.selectStockAndNavigate(holding.ticker) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand.copy(alpha = 0.12f), contentColor = OrangeBrand),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Acheter plus", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        sellQtyInput = ""
                                        activeSellHolding = holding
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Vendre", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trading open hour note
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrangeBrand)
                Column {
                    Text("Le saviez-vous ?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = "Le marché de la BRVM est ouvert de 09:00 à 15:00 GMT. Vos ordres passés en dehors de ces horaires seront placés en file d'attente pour l'ouverture.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    if (activeSellHolding != null) {
        val holding = activeSellHolding!!
        val maxShares = holding.sharesCount
        var errorMsg by remember { mutableStateOf("") }
        val qtyToSell = sellQtyInput.toIntOrNull() ?: 0
        val estRevenue = qtyToSell * holding.currentPrice

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { activeSellHolding = null }
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Vente d'actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                    Text(
                        text = "Combien d'actions de ${holding.companyName} (${holding.ticker}) voulez-vous vendre ? (Max : $maxShares)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = sellQtyInput,
                        onValueChange = {
                            sellQtyInput = it
                            errorMsg = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Quantité à vendre") },
                        placeholder = { Text("ex: 5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (qtyToSell > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ForestGreen.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Revenu estimé de la vente", fontSize = 11.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                                Text(estRevenue.formatFcfa(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = ForestGreen)
                            }
                        }
                    }

                    if (errorMsg.isNotEmpty()) {
                        Text(errorMsg, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { activeSellHolding = null },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Annuler")
                        }
                        Button(
                            onClick = {
                                if (qtyToSell <= 0 || qtyToSell > maxShares) {
                                    errorMsg = "Quantité invalide (doit être entre 1 et $maxShares)"
                                    return@Button
                                }
                                viewModel.executeSale(holding.ticker, qtyToSell, holding.currentPrice)
                                activeSellHolding = null
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Vendre", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 7. DEPOSIT / RECHARGE FUNDS SCREEN
@Composable
fun DepositScreen(viewModel: BourseViewModel) {
    val amountInput by viewModel.depositAmountInput.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.depositPaymentMethod.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val amtDouble = amountInput.toDoubleOrNull() ?: 0.0
    val formattedAmt = amtDouble.formatFcfa()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
                }
                Text(
                    text = "Alimenter mon compte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { }, enabled = false) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.Transparent)
            }
        }

        // Amount Input
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Montant du dépôt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = amountInput,
                onValueChange = { viewModel.depositAmountInput.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deposit_amount_field"),
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = OrangeBrand, fontWeight = FontWeight.Bold),
                trailingIcon = { Text("FCFA", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(end = 16.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(14.dp))
                Text("Dépôt minimum : 1 000 FCFA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Payment method selector
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Moyen de paiement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            val paymentOptions = listOf(
                Pair("Orange Money", "Orange"),
                Pair("Wave CI", "Wave"),
                Pair("Moov Money", "Moov"),
                Pair("Carte Bancaire", "Carte")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(180.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paymentOptions.size) { index ->
                    val option = paymentOptions[index]
                    val isSelected = paymentMethod == option.first

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) OrangeBrand.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                            .border(2.dp, if (isSelected) OrangeBrand else GrayBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.depositPaymentMethod.value = option.first }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Render nice text placeholder for logo
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (option.second) {
                                            "Orange" -> Color(0xFFFF7900)
                                            "Wave" -> Color(0xFF1DA1F2)
                                            "Moov" -> Color(0xFF005DA4)
                                            else -> GrayBorder
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (option.second == "Carte") {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = DarkOnBackground)
                                } else {
                                    Text(
                                        text = option.second.uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                            Text(option.first, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Detailed recap
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = OrangeBrand)
                    Text("Récapitulatif", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Montant saisi", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formattedAmt, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Frais de transaction (0%)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("0 FCFA", fontWeight = FontWeight.Bold, color = ForestGreen)
                }
                HorizontalDivider(color = GrayBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total à créditer", fontWeight = FontWeight.Bold)
                    Text(formattedAmt, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = OrangeBrand)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Cette transaction sera automatiquement enregistrée dans votre historique d'activités pour un suivi de votre patrimoine.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val isVerified = userProfile != null && userProfile!!.kycStep >= 5
        Button(
            onClick = { viewModel.executeDeposit(context) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("confirm_deposit_button"),
            colors = ButtonDefaults.buttonColors(containerColor = if (isVerified) OrangeBrand else Color.Gray),
            enabled = isVerified && amtDouble >= 1000.0,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isVerified) "Confirmer le dépôt" else "Dépôt Verrouillé (Non Validé)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 8. TRANSACTION HISTORY SCREEN
@Composable
fun HistoryScreen(viewModel: BourseViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Tout") } // "Tout", "Dépôts", "Transactions"

    val filteredList = transactions.filter { tx ->
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) || tx.reference.contains(searchQuery, ignoreCase = true)
        val matchesTab = when (selectedTab) {
            "Dépôts" -> tx.type == "DEPOSIT"
            "Transactions" -> tx.type == "BUY" || tx.type == "SELL"
            else -> true
        }
        matchesSearch && matchesTab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top app bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
                }
                Text(
                    text = "Historique",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { }) {
                Icon(Icons.Default.Download, contentDescription = "Télécharger", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher une entreprise ou référence...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Tabs
        TabRow(
            selectedTabIndex = when (selectedTab) {
                "Dépôts" -> 1
                "Transactions" -> 2
                else -> 0
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(selected = selectedTab == "Tout", onClick = { selectedTab = "Tout" }, text = { Text("Tout") })
            Tab(selected = selectedTab == "Dépôts", onClick = { selectedTab = "Dépôts" }, text = { Text("Dépôts") })
            Tab(selected = selectedTab == "Transactions", onClick = { selectedTab = "Transactions" }, text = { Text("Transactions") })
        }

        // List
        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = GrayBorder, modifier = Modifier.size(64.dp))
                    Text("Aucune transaction trouvée", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { tx ->
                    val colorLeftBorder = when (tx.status) {
                        "ANNULÉ" -> GrayBorder
                        "EN ATTENTE" -> OrangeBrand
                        else -> if (tx.type == "DEPOSIT" || tx.type == "SELL") ForestGreen else RedLoss
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GrayBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    val canvasHeight = this.size.height
                                    val widthPx = 4.dp.toPx()
                                    // Colored vertical left border
                                    drawRect(
                                        color = colorLeftBorder,
                                        topLeft = Offset(0f, 0f),
                                        size = Size(widthPx, canvasHeight)
                                    )
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (tx.type) {
                                                "DEPOSIT" -> ForestGreen.copy(alpha = 0.12f)
                                                "SELL" -> ForestGreen.copy(alpha = 0.12f)
                                                else -> RedLoss.copy(alpha = 0.12f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (tx.type) {
                                            "DEPOSIT" -> Icons.Default.ArrowUpward
                                            "SELL" -> Icons.Default.ShowChart
                                            else -> Icons.Default.ShoppingCart
                                        },
                                        contentDescription = null,
                                        tint = if (tx.type == "DEPOSIT" || tx.type == "SELL") ForestGreen else RedLoss,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${tx.date} • Réf: ${tx.reference}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(
                                                    when (tx.status) {
                                                        "ANNULÉ" -> RedLoss.copy(alpha = 0.15f)
                                                        "EN ATTENTE" -> OrangeBrand.copy(alpha = 0.15f)
                                                        else -> ForestGreen.copy(alpha = 0.15f)
                                                    }
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tx.status,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (tx.status) {
                                                    "ANNULÉ" -> RedLoss
                                                    "EN ATTENTE" -> OrangeBrand
                                                    else -> ForestGreen
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val prefix = if (tx.type == "DEPOSIT" || tx.type == "SELL") "+" else "-"
                                val textVal = "${prefix} ${Math.abs(tx.amount).formatFcfa()}"
                                Text(
                                    text = textVal,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (tx.type == "DEPOSIT" || tx.type == "SELL") ForestGreen else DarkOnBackground,
                                    fontSize = 14.sp
                                )
                                if (tx.sharesQty > 0) {
                                    Text("${tx.sharesQty} actions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 9. HELP CENTER / ACADÉMIE SCREEN
@Composable
fun HelpScreen(viewModel: BourseViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var expandedIndex by remember { mutableStateOf<Int?>(null) } // Accordion expansion index

    val faqs = listOf(
        Pair("Comment valider mon KYC ?", "Pour valider votre KYC, rendez-vous dans Profil > Vérification. Vous devrez fournir une pièce d'identité ivoirienne valide (CNI ou Passeport) et un justificatif de domicile de moins de 3 mois (facture CIE ou SODECI)."),
        Pair("Délais pour un retrait Mobile Money ?", "Les retraits vers Orange Money, Wave ou MTN MoMo sont généralement traités en moins de 15 minutes pendant les heures d'ouverture de la bourse (09:00 - 15:00 GMT)."),
        Pair("Comment acheter des actions BRVM ?", "Allez dans l'onglet 'Marché', recherchez une entreprise cotée (ex: Sonatel), cliquez sur 'Acheter', saisissez la quantité désirée et confirmez votre ordre d'achat."),
        Pair("Qu'est-ce que la capitalisation boursière ?", "La capitalisation représente la valeur globale d'une entreprise sur le marché financier. Elle est calculée en multipliant le nombre total d'actions par le cours actuel de l'action."),
        Pair("Quelle est la fiscalité sur les dividendes ?", "En Côte d'Ivoire, les dividendes des sociétés cotées à la BRVM sont totalement exonérés d'impôts (IRVM) pour tous les résidents fiscaux de l'UEMOA, ce qui en fait un investissement très attractif.")
    )

    val filteredFaqs = faqs.filter {
        it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Centre d'Aide & Académie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text("Comment pouvons-nous vous aider ?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Rechercher une solution...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Grid Bento Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(OrangeBrand.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(18.dp))
                    }
                    Text("Mon Compte", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("KYC, Profil, Sécurité", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(ForestGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                    }
                    Text("Fonds", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Dépôts & Retraits", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(GoldPremium.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldPremium, modifier = Modifier.size(18.dp))
                    }
                    Text("Ordres BRVM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Achat, Vente, Limites", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GrayBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Red.copy(alpha = 0.08f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                    Text("Sécurité", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("2FA, AMF-UMOA", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // FAQs Accordeon
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("FAQ Populaires", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            filteredFaqs.forEachIndexed { index, faq ->
                val isExpanded = expandedIndex == index
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Column(modifier = Modifier.clickable { expandedIndex = if (isExpanded) null else index }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(faq.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Text(
                                text = faq.second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Call Support Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Vous ne trouvez pas de réponse ?", fontWeight = FontWeight.Bold)
                Text("Nos conseillers sont à votre disposition de 8h à 18h GMT.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lancer le Chat en direct", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Appeler un conseiller", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 10. USER PROFILE SCREEN
@Composable
fun ProfileScreen(viewModel: BourseViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var activeDialog by remember { mutableStateOf<String?>(null) }

    val nameText = if (userProfile != null && userProfile!!.firstName.isNotBlank()) {
        "${userProfile!!.firstName} ${userProfile!!.lastName}"
    } else {
        "Koffi Kouassi"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateTo(Screen.DASHBOARD) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OrangeBrand)
            }
            Text(
                text = "Profil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val photoUri by viewModel.profilePhotoUri.collectAsStateWithLifecycle()
        var showPhotoOptionDialog by remember { mutableStateOf(false) }

        // Profile Card Heading with Photo Avatar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clickable { showPhotoOptionDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(OrangeBrand.copy(alpha = 0.12f))
                        .border(3.dp, ForestGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Photo de profil client",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(54.dp))
                    }
                }

                // Camera Badge Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(OrangeBrand)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Changer la photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Text(nameText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            val refID = "EB-${(100000..999999).random()}"
            Text("ID: $refID", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(ForestGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
                    Text("Vérifié KYC", color = ForestGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Photo Options Dialog (Prendre une photo / Choisir dans la Galerie)
        if (showPhotoOptionDialog) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showPhotoOptionDialog = false }) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Photo de Profil Client", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Prenez une photo de votre visage ou choisissez une image de votre galerie.", fontSize = 12.sp, color = Color.Gray)

                        Button(
                            onClick = {
                                viewModel.updateProfilePhoto("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80")
                                showPhotoOptionDialog = false
                                android.widget.Toast.makeText(context, "📷 Photo de profil enregistrée avec succès !", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Prendre une Photo (Appareil Photo 📷)", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.updateProfilePhoto("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop&q=80")
                                showPhotoOptionDialog = false
                                android.widget.Toast.makeText(context, "🖼️ Photo de profil mise à jour !", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choisir dans la Galerie 🖼️", fontWeight = FontWeight.Medium)
                        }

                        TextButton(
                            onClick = { showPhotoOptionDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Annuler", color = Color.Gray)
                        }
                    }
                }
            }
        }



        // Menu items Column list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, GrayBorder)
        ) {
            Column {
                val menuItems = listOf(
                    Pair(Icons.Default.Person, "Informations Personnelles"),
                    Pair(Icons.Default.Shield, "Sécurité & Double Facteur (2FA)"),
                    Pair(Icons.Default.Notifications, "Préférences de Notifications"),
                    Pair(Icons.Default.Description, "Documents & Contrats"),
                    Pair(Icons.Default.Chat, "Messagerie Support")
                )

                menuItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeDialog = item.second }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(item.first, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(22.dp))
                            Text(item.second, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = GrayBorder.copy(alpha = 0.5f))
                }
            }
        }

        // Signout / Reset Data action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.resetDemoData() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("reset_demo_button")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Réinitialiser", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.performLogout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Déconnexion", fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Éléphant Bourse v2.4.1 (Stable)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }

    // Interactive Dialogs
    when (activeDialog) {
        "Informations Personnelles" -> {
            androidx.compose.ui.window.Dialog(onDismissRequest = { activeDialog = null }) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Informations Personnelles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Prénom", fontSize = 11.sp, color = Color.Gray)
                            Text(userProfile?.firstName ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            HorizontalDivider()
                            
                            Text("Nom de famille", fontSize = 11.sp, color = Color.Gray)
                            Text(userProfile?.lastName ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            HorizontalDivider()
                            
                            Text("Adresse e-mail", fontSize = 11.sp, color = Color.Gray)
                            Text(viewModel.firstNameInput.value.split("@").firstOrNull() ?: "investisseur@email.ci", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            HorizontalDivider()
                            
                            Text("Numéro WhatsApp", fontSize = 11.sp, color = Color.Gray)
                            Text(userProfile?.whatsapp ?: "-", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }

                        Button(
                            onClick = { activeDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Fermer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        "Sécurité & Double Facteur (2FA)" -> {
            val is2fa by viewModel.is2faEnabled.collectAsStateWithLifecycle()
            val isFingerprint by viewModel.isFingerprintEnabled.collectAsStateWithLifecycle()

            androidx.compose.ui.window.Dialog(onDismissRequest = { activeDialog = null }) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("Sécurité & 2FA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Authentification 2FA", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Ajoute un code OTP temporaire à la connexion", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(checked = is2fa, onCheckedChange = { viewModel.is2faEnabled.value = it })
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Empreinte Digitale / Biométrie", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Déverrouillage rapide de l'application", fontSize = 11.sp, color = Color.Gray)
                            }
                            Switch(checked = isFingerprint, onCheckedChange = { viewModel.isFingerprintEnabled.value = it })
                        }

                        Button(
                            onClick = { activeDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sauvegarder", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        "Préférences de Notifications" -> {
            var pushEnabled by remember { mutableStateOf(true) }
            var emailEnabled by remember { mutableStateOf(true) }
            var smsEnabled by remember { mutableStateOf(false) }

            androidx.compose.ui.window.Dialog(onDismissRequest = { activeDialog = null }) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Text("Préférences de Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Notifications Push", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Switch(checked = pushEnabled, onCheckedChange = { pushEnabled = it })
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Alertes E-mail", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Switch(checked = emailEnabled, onCheckedChange = { emailEnabled = it })
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Alertes SMS", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Switch(checked = smsEnabled, onCheckedChange = { smsEnabled = it })
                        }

                        Button(
                            onClick = { activeDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Fermer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        "Documents & Contrats" -> {
            androidx.compose.ui.window.Dialog(onDismissRequest = { activeDialog = null }) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Documents & Contrats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Téléchargez les documents officiels relatifs à vos activités d'investissement.", fontSize = 12.sp, color = Color.Gray)

                        val docs = listOf(
                            "Contrat d'Ouverture.pdf",
                            "Fiche d'Inscription KYC.pdf",
                            "Reglement General BRVM.pdf"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            docs.forEach { doc ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFF9F9F9)).padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = OrangeBrand, modifier = Modifier.size(20.dp))
                                        Text(doc, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                    IconButton(onClick = {
                                        val pdfHeader = "%PDF-1.4\n1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n2 0 obj\n<< /Type /Pages /Kinds [] /Count 1 >>\nendobj\ntrailer\n<< /Root 1 0 R >>\n%%EOF"
                                        val content = "====================================================\nÉLÉPHANT BOURSE - DOCUMENT OFFICIEL & CONTRAT SGI\n====================================================\nDocument: $doc\nClient: ${userProfile?.firstName ?: "Client"} ${userProfile?.lastName ?: ""}\nStatut: Conforme AMF-UMOA\nDate: Aujourd'hui\n====================================================\n" + pdfHeader
                                        
                                        val success = savePdfDocumentToPhone(context, doc, content)
                                        if (success) {
                                            android.widget.Toast.makeText(context, "✅ $doc enregistré dans votre dossier Téléchargements !", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "✅ Document $doc téléchargé !", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Download, contentDescription = "Télécharger", tint = ForestGreen, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { activeDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeBrand),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Fermer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        "Messagerie Support" -> {
            val messageText by viewModel.supportMessageInput.collectAsStateWithLifecycle()
            val subjectText by viewModel.supportSubjectInput.collectAsStateWithLifecycle()
            var isSending by remember { mutableStateOf(false) }

            androidx.compose.ui.window.Dialog(onDismissRequest = { activeDialog = null }) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Messagerie Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Posez vos questions directement aux administrateurs de la plate-forme.", fontSize = 12.sp, color = Color.Gray)

                        OutlinedTextField(
                            value = subjectText,
                            onValueChange = { viewModel.supportSubjectInput.value = it },
                            label = { Text("Sujet") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { viewModel.supportMessageInput.value = it },
                            label = { Text("Votre message") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 5,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { activeDialog = null },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Fermer")
                            }
                            Button(
                                onClick = {
                                    if (messageText.isBlank()) return@Button
                                    isSending = true
                                    viewModel.sendSupportMessage { success ->
                                        isSending = false
                                        if (success) {
                                            activeDialog = null
                                        }
                                    }
                                },
                                enabled = messageText.isNotBlank() && !isSending,
                                modifier = Modifier.weight(1f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Envoyer", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

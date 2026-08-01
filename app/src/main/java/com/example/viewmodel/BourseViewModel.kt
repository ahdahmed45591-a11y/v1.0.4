package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.BourseRepository
import com.example.data.local.AppDatabase
import com.example.data.local.HoldingsEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    WELCOME,
    ONBOARDING,
    SIGNATURE,
    DASHBOARD,
    MARKET,
    PORTFOLIO,
    DEPOSIT,
    HISTORY,
    HELP,
    PROFILE
}

data class StockWatchItem(
    val ticker: String,
    val companyName: String,
    val sector: String,
    val price: Double,
    val changePercent: Double,
    val isGaining: Boolean = changePercent >= 0
)

class BourseViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "bourse_db"
    )
    .fallbackToDestructiveMigration()
    .build()

    private val repository = BourseRepository(database.bourseDao())

    // Screen navigation state
    private val _currentScreen = MutableStateFlow(Screen.WELCOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen navigation stack to support going back (simplifies single activity routing)
    private val screenHistory = mutableListOf<Screen>()

    // Profile Photo State
    val profilePhotoUri = MutableStateFlow<String?>(null)

    fun updateProfilePhoto(uriString: String) {
        profilePhotoUri.value = uriString
    }

    // Selected stock for Market Detail View
    private val _selectedStock = MutableStateFlow<StockWatchItem?>(null)
    val selectedStock: StateFlow<StockWatchItem?> = _selectedStock.asStateFlow()

    // Observable Flows from Room
    val userProfile: StateFlow<UserEntity?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val holdings: StateFlow<List<HoldingsEntity>> = repository.allHoldings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Watchlist stock constants — Liste complète des sociétés cotées à la BRVM
    val watchlist = listOf(
        StockWatchItem("SNTS", "Sonatel CI", "Télécoms", 16850.0, 2.45),
        StockWatchItem("ORAC", "Orange CI", "Télécoms", 10450.0, -0.85),
        StockWatchItem("ONTBF", "Onatel BF", "Télécoms", 2695.0, -1.82),
        StockWatchItem("SGBC", "Société Générale CI", "Banque", 37995.0, -0.04),
        StockWatchItem("ETI", "Ecobank Transnational", "Banque", 73.0, -6.41),
        StockWatchItem("BOAB", "BOA Bénin", "Banque", 8780.0, 0.17),
        StockWatchItem("BOAC", "BOA Côte d'Ivoire", "Banque", 7200.0, 1.41),
        StockWatchItem("BOABF", "BOA Burkina Faso", "Banque", 6850.0, 0.74),
        StockWatchItem("BOAM", "BOA Mali", "Banque", 1850.0, 0.0),
        StockWatchItem("BOAN", "BOA Niger", "Banque", 4900.0, 0.50),
        StockWatchItem("BOAS", "BOA Sénégal", "Banque", 3200.0, 3.10),
        StockWatchItem("BICI", "BICICI", "Banque", 7800.0, 1.04),
        StockWatchItem("SIB", "Société Ivoirienne de Banque", "Banque", 5400.0, 0.93),
        StockWatchItem("NSBC", "NSIA Banque CI", "Banque", 6100.0, -0.50),
        StockWatchItem("CBIB", "Coris Bank International BF", "Banque", 10200.0, 1.20),
        StockWatchItem("PALC", "Palm CI", "Agriculture", 7100.0, 2.15),
        StockWatchItem("SOGC", "SOGB CI", "Agriculture", 4200.0, 1.85),
        StockWatchItem("SAPC", "SAPH CI", "Agriculture", 3400.0, -0.75),
        StockWatchItem("SLBC", "Solibra CI", "Industrie", 89000.0, 0.0),
        StockWatchItem("UNXC", "Uniwax CI", "Industrie", 780.0, -1.25),
        StockWatchItem("CABC", "Sicable CI", "Industrie", 3625.0, -2.29),
        StockWatchItem("CIEC", "CIE Côte d'Ivoire", "Services Publics", 2150.0, 0.45),
        StockWatchItem("SDCC", "SODECI Côte d'Ivoire", "Services Publics", 5300.0, 1.10),
        StockWatchItem("CFAC", "CFAO Motors CI", "Distribution", 920.0, 0.0),
        StockWatchItem("TTLS", "TotalEnergies Marketing SN", "Distribution", 2500.0, 0.80),
        StockWatchItem("SDSC", "AGL / Bolloré Transport CI", "Transport", 1650.0, -0.60)
    )

    // KYC Document Capture State Variables
    val identityRectoStatus = MutableStateFlow<String?>("Non Fourni")
    val identityVersoStatus = MutableStateFlow<String?>("Non Fourni")
    val selfiePhotoStatus = MutableStateFlow<String?>("Non Fourni")
    val proofOfAddressStatus = MutableStateFlow<String?>("Non Fourni")

    fun captureIdentityRecto() {
        identityRectoStatus.value = "Recto CNI/Passeport Scanné ✅"
    }

    fun captureIdentityVerso() {
        identityVersoStatus.value = "Verso CNI Scanné ✅"
    }

    fun captureSelfiePhoto() {
        selfiePhotoStatus.value = "Selfie Visage Validé ✅"
    }

    fun captureProofOfAddressDocument() {
        proofOfAddressStatus.value = "Facture CIE/SODECI Chargée ✅"
    }

    // Order state variables (bound to view)
    val orderQuantity = MutableStateFlow("10")
    val orderLimitPrice = MutableStateFlow("16850")
    val orderTypeIsMarket = MutableStateFlow(true) // true = Marché, false = Limite

    // Deposit state variables
    val depositAmountInput = MutableStateFlow("")
    val depositPaymentMethod = MutableStateFlow("Orange Money")

    // Onboarding form state
    val firstNameInput = MutableStateFlow("")
    val lastNameInput = MutableStateFlow("")
    val birthDateInput = MutableStateFlow("")
    val professionInput = MutableStateFlow("")
    val residenceInput = MutableStateFlow("Abidjan, Côte d'Ivoire")
    val onboardingStep = MutableStateFlow(1) // 1 = Personal Details, 2 = Biometrics KYC, 3 = Proof of Address

    // SMS contract validation variables
    val smsOtpCode = MutableStateFlow(listOf("", "", "", "", "", ""))
    val signatureBytes = MutableStateFlow<ByteArray?>(null)

    val showGoogleAccountChooser = MutableStateFlow(false)
    val showServerUrlSettings = MutableStateFlow(false)
    val serverUrlInput = MutableStateFlow("http://10.0.2.2:3001/api/")

    fun initializeServerUrl(context: android.content.Context) {
        val prefs = context.getSharedPreferences("baou_prefs", android.content.Context.MODE_PRIVATE)
        val savedUrl = prefs.getString("server_url", "http://10.0.2.2:3001/api/") ?: "http://10.0.2.2:3001/api/"
        serverUrlInput.value = savedUrl
        com.example.data.network.ApiClient.updateBaseUrl(savedUrl)
    }

    fun saveServerUrl(url: String, context: android.content.Context) {
        val prefs = context.getSharedPreferences("baou_prefs", android.content.Context.MODE_PRIVATE)
        com.example.data.network.ApiClient.updateBaseUrl(url)
        val sanitized = com.example.data.network.ApiClient.getBaseUrl()
        prefs.edit().putString("server_url", sanitized).apply()
        serverUrlInput.value = sanitized
        viewModelScope.launch {
            _transactionStatus.emit("Adresse du serveur configurée !")
        }
    }

    // Additional registration, support & security states
    val whatsappInput = MutableStateFlow("")
    val is2faEnabled = MutableStateFlow(false)
    val isFingerprintEnabled = MutableStateFlow(false)
    val supportSubjectInput = MutableStateFlow("Assistance Client")
    val supportMessageInput = MutableStateFlow("")

    // General transaction status / toast message
    private val _transactionStatus = MutableSharedFlow<String>()
    val transactionStatus: SharedFlow<String> = _transactionStatus.asSharedFlow()

    fun performRegister(email: String, password: String, firstName: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.register(email, password, firstName)
            if (result == "SUCCESS") {
                // Auto login after successful registration
                val loginResult = repository.login(email, password)
                if (loginResult == "SUCCESS") {
                    val context = getApplication<Application>().applicationContext
                    val prefs = context.getSharedPreferences("baou_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("auth_email", email)
                        .putString("auth_password", password)
                        .apply()
                    _transactionStatus.emit("Inscription et connexion réussies !")
                    onComplete(null)
                } else {
                    onComplete("Créé avec succès, mais échec de connexion automatique: $loginResult")
                }
            } else {
                onComplete(result)
            }
        }
    }

    fun sendSupportMessage(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val msg = supportMessageInput.value
            val subj = supportSubjectInput.value
            if (msg.isNotBlank()) {
                val success = repository.sendSupportMessage(subj, msg)
                if (success) {
                    _transactionStatus.emit("Message envoyé à l'administrateur !")
                    supportMessageInput.value = ""
                    onComplete(true)
                } else {
                    _transactionStatus.emit("Échec de l'envoi du message.")
                    onComplete(false)
                }
            } else {
                onComplete(false)
            }
        }
    }

    fun performLogin(email: String = "mamadou.konate@email.ci", password: String = "password123", onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.login(email, password)
            if (result == "SUCCESS") {
                val context = getApplication<Application>().applicationContext
                val prefs = context.getSharedPreferences("baou_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("auth_email", email)
                    .putString("auth_password", password)
                    .apply()
                _transactionStatus.emit("Connecté au serveur Express local !")
                onComplete(true)
            } else {
                _transactionStatus.emit("Connexion échouée: $result. Utilisation du mode local.")
                onComplete(false)
            }
        }
    }

    fun syncWithBackend() {
        viewModelScope.launch {
            val success = repository.syncTransactions()
            if (success) {
                _transactionStatus.emit("Transactions synchronisées avec succès.")
            } else {
                _transactionStatus.emit("Échec de la synchronisation.")
            }
        }
    }

    init {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val prefs = context.getSharedPreferences("baou_prefs", android.content.Context.MODE_PRIVATE)
            
            // Initialize Base URL before any repository call
            val savedUrl = prefs.getString("server_url", "http://10.0.2.2:3001/api/") ?: "http://10.0.2.2:3001/api/"
            serverUrlInput.value = savedUrl
            com.example.data.network.ApiClient.updateBaseUrl(savedUrl)

            // Seed database if empty
            repository.initializeDefaultData()
            
            // Check if user is already signed up and verified to auto-skip splash
            val profile = repository.userProfile.first()
            if (profile != null && profile.kycStep >= 5) {
                val savedEmail = prefs.getString("auth_email", "") ?: ""
                val savedPassword = prefs.getString("auth_password", "") ?: ""
                if (savedEmail.isNotEmpty() && savedPassword.isNotEmpty()) {
                    repository.login(savedEmail, savedPassword)
                } else {
                    repository.login("mamadou.konate@email.ci", "password123")
                }
                _currentScreen.value = Screen.DASHBOARD
            }
        }
    }

    // Navigation Methods
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
        } else {
            _currentScreen.value = Screen.WELCOME
        }
    }

    fun selectStockAndNavigate(ticker: String) {
        val stock = watchlist.find { it.ticker == ticker }
        if (stock != null) {
            _selectedStock.value = stock
            orderLimitPrice.value = stock.price.toInt().toString()
            navigateTo(Screen.MARKET)
        }
    }

    // Onboarding Form actions
    fun submitPersonalDetails() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(
                firstName = firstNameInput.value,
                lastName = lastNameInput.value,
                birthDate = birthDateInput.value,
                whatsapp = whatsappInput.value,
                kycStep = 2 // Move to Identity validation step
            )
            repository.saveUserProfile(updated)
            repository.updateBackendProfile(
                firstNameInput.value,
                lastNameInput.value,
                "pending",
                whatsappInput.value,
                birthDateInput.value,
                professionInput.value,
                residenceInput.value
            )
            onboardingStep.value = 2
        }
    }

    fun completeIdentityBiometrics() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(kycStep = 3) // Move to Proof of address
            repository.saveUserProfile(updated)
            repository.updateBackendProfile(
                profile.firstName,
                profile.lastName,
                "pending",
                profile.whatsapp,
                profile.birthDate,
                professionInput.value,
                residenceInput.value,
                identityDocStatus = "CNI (Recto & Verso) + Selfie Chargés ✅",
                proofOfAddressStatus = proofOfAddressStatus.value ?: "Non Fourni",
                signatureStatus = "En attente"
            )
            onboardingStep.value = 3
        }
    }

    fun completeAddressUpload() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(kycStep = 4) // Move to Legal Signature
            repository.saveUserProfile(updated)
            repository.updateBackendProfile(
                profile.firstName,
                profile.lastName,
                "pending",
                profile.whatsapp,
                profile.birthDate,
                professionInput.value,
                residenceInput.value,
                identityDocStatus = "CNI (Recto & Verso) + Selfie Chargés ✅",
                proofOfAddressStatus = "Facture CIE / SODECI Chargée ✅",
                signatureStatus = "En attente"
            )
            navigateTo(Screen.SIGNATURE)
        }
    }

    fun verifySmsOtp() {
        viewModelScope.launch {
            val profile = repository.userProfile.first() ?: return@launch
            val updated = profile.copy(kycStep = 5) // Fully verified
            repository.saveUserProfile(updated)
            repository.updateBackendProfile(profile.firstName, profile.lastName, "verified", profile.whatsapp)
            _transactionStatus.emit("Inscription validée avec succès !")
            repository.syncTransactions() // Synchroniser après vérification
            navigateTo(Screen.DASHBOARD)
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val prefs = context.getSharedPreferences("baou_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .remove("auth_email")
                .remove("auth_password")
                .apply()

            val resetProfile = UserEntity(
                firstName = "",
                lastName = "",
                birthDate = "",
                whatsapp = "",
                kycStep = 0,
                cashBalance = 0.0,
                portfolioValue = 0.0
            )
            repository.saveUserProfile(resetProfile)
            onboardingStep.value = 1
            firstNameInput.value = ""
            lastNameInput.value = ""
            birthDateInput.value = ""
            _currentScreen.value = Screen.WELCOME
            screenHistory.clear()
            _transactionStatus.emit("Déconnecté avec succès.")
        }
    }

    // Reset profile back to standard demo values
    fun resetDemoData() {
        viewModelScope.launch {
            val profile = repository.userProfile.first()
            if (profile != null) {
                val resetProfile = UserEntity(
                    firstName = "",
                    lastName = "",
                    birthDate = "",
                    whatsapp = "",
                    kycStep = 0,
                    cashBalance = 0.0,
                    portfolioValue = 0.0
                )
                repository.saveUserProfile(resetProfile)
                onboardingStep.value = 1
                firstNameInput.value = ""
                lastNameInput.value = ""
                birthDateInput.value = ""
                _currentScreen.value = Screen.WELCOME
                screenHistory.clear()
            }
        }
    }

    val pendingWaveDepositState = MutableStateFlow<Double>(0.0)

    // Transaction Actions
    fun executeDeposit(context: android.content.Context) {
        val profile = userProfile.value
        if (profile == null || profile.kycStep < 5) {
            viewModelScope.launch {
                _transactionStatus.emit("Action verrouillée : Votre dossier est en cours de validation par l'admin. Vous pourrez effectuer des dépôts dès sa validation.")
            }
            return
        }

        val amt = depositAmountInput.value.toDoubleOrNull()
        if (amt == null || amt <= 0) {
            viewModelScope.launch {
                _transactionStatus.emit("Veuillez saisir un montant valide pour le dépôt.")
            }
            return
        }

        val isWave = depositPaymentMethod.value == "Wave CI"

        if (isWave) {
            pendingWaveDepositState.value = amt
            depositAmountInput.value = ""

            try {
                val redirectUrl = android.net.Uri.encode("baou://payment/success?status=success&amount=${amt.toInt()}")
                val waveUrl = "https://pay.wave.com/m/M_ci_XRkfDq_9M8GP/c/ci/?src=p&amount=${amt.toInt()}&redirect_url=$redirectUrl"

                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(waveUrl))
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)

                viewModelScope.launch {
                    _transactionStatus.emit("📱 Ouverture du compte marchand Wave (${amt.toInt()} FCFA)... Confirmez le paiement pour crédit immédiat.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                viewModelScope.launch {
                    _transactionStatus.emit("Veuillez finaliser le paiement dans Wave puis revenir valider.")
                }
            }
        } else {
            viewModelScope.launch {
                val success = repository.depositFunds(amt, depositPaymentMethod.value)
                if (success) {
                    depositAmountInput.value = ""
                    _transactionStatus.emit("Dépôt de ${amt.toInt()} FCFA effectué avec succès !")
                    repository.syncTransactions()
                    navigateTo(Screen.PORTFOLIO)
                } else {
                    _transactionStatus.emit("Échec du dépôt.")
                }
            }
        }
    }

    fun confirmWaveDeposit(amountOverride: Double? = null) {
        val amtToCredit = amountOverride ?: pendingWaveDepositState.value
        if (amtToCredit <= 0) return

        pendingWaveDepositState.value = 0.0

        viewModelScope.launch {
            val success = repository.depositFunds(amtToCredit, "Wave CI")
            if (success) {
                _transactionStatus.emit("🎉 Dépôt Wave de ${amtToCredit.toInt()} FCFA confirmé ! Votre portefeuille a été mis à jour.")
                repository.syncTransactions()
                navigateTo(Screen.PORTFOLIO)
            } else {
                _transactionStatus.emit("Erreur lors de la confirmation du dépôt Wave.")
            }
        }
    }

    fun executeOrder() {
        val profile = userProfile.value
        if (profile == null || profile.kycStep < 5) {
            viewModelScope.launch {
                _transactionStatus.emit("Action verrouillée : Votre dossier est en cours de validation par l'admin. Vous pourrez acheter des actions dès sa validation.")
            }
            return
        }

        val qty = orderQuantity.value.toIntOrNull() ?: 0
        if (qty <= 0) {
            viewModelScope.launch { _transactionStatus.emit("Veuillez saisir une quantité valide.") }
            return
        }

        val stock = _selectedStock.value ?: return
        val price = if (orderTypeIsMarket.value) stock.price else (orderLimitPrice.value.toDoubleOrNull() ?: stock.price)

        viewModelScope.launch {
            val result = repository.buyStock(
                ticker = stock.ticker,
                companyName = stock.companyName,
                sharesQty = qty,
                price = price,
                sector = stock.sector
            )

            if (result == "SUCCESS") {
                _transactionStatus.emit("Achat de $qty actions ${stock.companyName} effectué !")
                navigateTo(Screen.DASHBOARD)
            } else {
                _transactionStatus.emit(result)
            }
        }
    }

    fun executeSale(ticker: String, sharesQty: Int, price: Double) {
        viewModelScope.launch {
            val result = repository.sellStock(ticker, sharesQty, price)
            if (result == "SUCCESS") {
                _transactionStatus.emit("Vente de $sharesQty actions effectuée !")
            } else {
                _transactionStatus.emit(result)
            }
        }
    }

    // Real-time Chat & Messaging State between Client and Admin
    val chatMessagesState = MutableStateFlow<List<com.example.data.network.ChatMessage>>(emptyList())

    init {
        // Poll for chat messages sent by Admin every 4 seconds
        viewModelScope.launch {
            while (true) {
                try {
                    val token = repository.getAuthToken()
                    if (token != null) {
                        val response = com.example.data.network.ApiClient.service.getChatMessages(token)
                        if (response.isSuccessful) {
                            val body = response.body()
                            val data = body?.get("data") as? List<Map<String, Any>>
                            if (data != null) {
                                val list = data.map { map ->
                                    com.example.data.network.ChatMessage(
                                        id = map["id"]?.toString(),
                                        userId = map["userId"]?.toString(),
                                        userName = map["userName"]?.toString(),
                                        sender = map["sender"]?.toString() ?: "CLIENT",
                                        text = map["text"]?.toString() ?: "",
                                        time = map["time"]?.toString() ?: ""
                                    )
                                }
                                chatMessagesState.value = list
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fail silently for offline demo
                }
                kotlinx.coroutines.delay(4000)
            }
        }
    }

    fun uploadDocument(docType: String, fileName: String, fileBase64: String) {
        viewModelScope.launch {
            val success = repository.uploadDocument(docType, fileName, fileBase64)
            if (success) {
                _transactionStatus.emit("📄 Document $fileName transmis avec succès à l'administration !")
            }
        }
    }

    fun refreshAfterWavePayment() {
        confirmWaveDeposit()
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val currentList = chatMessagesState.value.toMutableList()
            currentList.add(com.example.data.network.ChatMessage(sender = "CLIENT", text = text, time = "À l'instant"))
            chatMessagesState.value = currentList
            repository.sendChatMessage(text)
        }
    }
}

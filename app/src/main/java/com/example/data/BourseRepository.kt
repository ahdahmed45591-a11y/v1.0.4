package com.example.data

import com.example.data.local.BourseDao
import com.example.data.local.HoldingsEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class BourseRepository(private val bourseDao: BourseDao) {

    val userProfile: Flow<UserEntity?> = bourseDao.getUserProfileFlow()
    val allTransactions: Flow<List<TransactionEntity>> = bourseDao.getAllTransactionsFlow()
    val allHoldings: Flow<List<HoldingsEntity>> = bourseDao.getAllHoldingsFlow()

    private var token: String? = null

    fun setToken(authToken: String) {
        token = "Bearer $authToken"
    }

    fun getAuthToken(): String? = token


    suspend fun login(email: String, password: String): String {
        return try {
            val response = com.example.data.network.ApiClient.service.login(
                com.example.data.network.LoginRequest(email, password)
            )
            if (response.success) {
                setToken(response.token)
                
                // Mettre à jour l'utilisateur local avec les infos de l'API
                val localUser = UserEntity(
                    id = 1,
                    firstName = response.user.name.substringBefore(" "),
                    lastName = response.user.name.substringAfter(" ", ""),
                    birthDate = "",
                    whatsapp = "", // Updated dynamically during KYC onboarding
                    kycStep = if (response.user.kyc == "verified") 5 else 1,
                    cashBalance = response.user.balance ?: 0.0,
                    portfolioValue = 0.0,
                    isPremium = response.user.type == "Premium" || response.user.role == "admin",
                    membershipDate = response.user.joinedAt ?: "Janvier 2023"
                )
                bourseDao.insertUserProfile(localUser)
                syncTransactions()
                "SUCCESS"
            } else {
                response.message ?: "Échec de connexion."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Erreur réseau: ${e.localizedMessage}"
        }
    }

    suspend fun updateBackendProfile(
        firstName: String,
        lastName: String,
        kycStatus: String,
        whatsapp: String? = null,
        birthDate: String? = null,
        profession: String? = null,
        residence: String? = null,
        identityDocStatus: String? = null,
        proofOfAddressStatus: String? = null,
        signatureStatus: String? = null
    ): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = com.example.data.network.ApiClient.service.updateProfile(
                currentToken,
                com.example.data.network.UpdateProfileRequest(
                    firstName,
                    lastName,
                    kycStatus,
                    whatsapp,
                    birthDate,
                    profession,
                    residence,
                    identityDocStatus,
                    proofOfAddressStatus,
                    signatureStatus
                )
            )
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun register(email: String, password: String, firstName: String): String {
        return try {
            val response = com.example.data.network.ApiClient.service.register(
                com.example.data.network.RegisterRequest(email, password, firstName)
            )
            if (response.isSuccessful) {
                "SUCCESS"
            } else {
                val errorBodyString = response.errorBody()?.string() ?: ""
                val cleanErrorMsg = try {
                    val jsonObj = org.json.JSONObject(errorBodyString)
                    jsonObj.optString("error", jsonObj.optString("message", errorBodyString))
                } catch (e: Exception) {
                    errorBodyString
                }
                cleanErrorMsg.ifEmpty { "Échec (code ${response.code()})" }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            "Erreur réseau: ${e.localizedMessage}"
        }
    }

    suspend fun sendSupportMessage(subject: String, message: String): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = com.example.data.network.ApiClient.service.sendSupport(
                currentToken,
                com.example.data.network.SupportRequest(subject, message)
            )
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun syncTransactions(): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = com.example.data.network.ApiClient.service.getTransactions(currentToken)
            if (response.success && response.data != null) {
                // Convertir les transactions réseau en entités locales de façon 100% sécurisée (sans crash si un champ est null)
                val localTxs = response.data.map { netTx ->
                    val statusFmt = when (netTx.status) {
                        "validated" -> "TERMINÉ"
                        "rejected" -> "ANNULÉ"
                        else -> "EN ATTENTE"
                    }
                    val txTotal = netTx.total
                    val amountFmt = if (netTx.type == "BUY") -txTotal else txTotal
                    val companyStr = netTx.company ?: netTx.ticker
                    val titleFmt = when (netTx.type) {
                        "BUY" -> "Achat $companyStr"
                        "SELL" -> "Vente $companyStr"
                        "DEPOSIT", "RECHARGE" -> if (companyStr.isEmpty()) "Dépôt ${netTx.paymentMethod ?: "Wave"}" else companyStr
                        else -> "Transaction $companyStr"
                    }
                    TransactionEntity(
                        type = if (netTx.type == "RECHARGE") "DEPOSIT" else netTx.type,
                        title = titleFmt,
                        date = "Aujourd'hui",
                        reference = netTx.id.ifEmpty { "REF-${(100000..999999).random()}" },
                        status = statusFmt,
                        amount = amountFmt,
                        sharesQty = netTx.quantity,
                        stockTicker = netTx.ticker
                    )
                }

                // Mettre à jour Room
                bourseDao.clearAllTransactions()
                for (tx in localTxs) {
                    bourseDao.insertTransaction(tx)
                }

                // Recalculer le solde et les positions d'après le serveur
                val profile = bourseDao.getUserProfile()
                if (profile != null) {
                    var balance = profile.cashBalance

                    // On recalcule les positions locales d'après les transactions validées
                    bourseDao.clearAllHoldings()
                    val holdingsMap = mutableMapOf<String, HoldingsEntity>()

                    val validatedTxs = response.data.filter { it.status == "validated" }
                    if (validatedTxs.isNotEmpty()) {
                        var calculatedBalance = 0.0
                        for (netTx in response.data) {
                            if (netTx.status == "validated") {
                                val ticker = netTx.ticker.ifEmpty { "UNKN" }
                                val company = netTx.company ?: ticker
                                val txGrandTotal = netTx.grandTotal ?: netTx.total

                                if (netTx.type == "BUY") {
                                    calculatedBalance -= txGrandTotal
                                    val existing = holdingsMap[ticker]
                                    if (existing != null) {
                                        val newQty = existing.sharesCount + netTx.quantity
                                        holdingsMap[ticker] = existing.copy(
                                            sharesCount = newQty,
                                            currentPrice = netTx.price
                                        )
                                    } else {
                                        holdingsMap[ticker] = HoldingsEntity(
                                            ticker = ticker,
                                            companyName = company,
                                            sharesCount = netTx.quantity,
                                            averagePrice = netTx.price,
                                            currentPrice = netTx.price,
                                            changePercent = 1.25,
                                            sector = "Bourse"
                                        )
                                    }
                                } else if (netTx.type == "SELL") {
                                    calculatedBalance += netTx.total
                                    val existing = holdingsMap[ticker]
                                    if (existing != null) {
                                        val newQty = existing.sharesCount - netTx.quantity
                                        if (newQty <= 0) {
                                            holdingsMap.remove(ticker)
                                        } else {
                                            holdingsMap[ticker] = existing.copy(sharesCount = newQty)
                                        }
                                    }
                                } else if (netTx.type == "DEPOSIT" || netTx.type == "RECHARGE") {
                                    calculatedBalance += netTx.total
                                }
                            }
                        }
                        if (calculatedBalance > 0) {
                            balance = calculatedBalance
                        }
                    }

                    // Ré-insérer les positions calculées
                    for (holding in holdingsMap.values) {
                        bourseDao.insertHolding(holding)
                    }

                    bourseDao.insertUserProfile(profile.copy(
                        cashBalance = balance,
                        portfolioValue = holdingsMap.values.sumOf { it.sharesCount * it.currentPrice }
                    ))
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun initializeDefaultData() {
        val currentProfile = bourseDao.getUserProfile()
        if (currentProfile == null) {
            val defaultUser = UserEntity(
                firstName = "",
                lastName = "",
                birthDate = "",
                kycStep = 0,
                cashBalance = 0.0,
                portfolioValue = 0.0,
                isPremium = false,
                membershipDate = ""
            )
            bourseDao.insertUserProfile(defaultUser)
        }
    }

    suspend fun saveUserProfile(user: UserEntity) {
        bourseDao.insertUserProfile(user)
    }

    suspend fun depositFunds(amount: Double, paymentMethod: String): Boolean {
        if (amount <= 0) return false

        val dateFormat = SimpleDateFormat("Aujourd'hui, HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        val reference = "REF-" + (100000..999999).random()

        // 1. Mettre à jour le solde cash local
        //    Si le profil n'existe pas encore en DB (premier dépôt avant sync),
        //    on crée un profil par défaut avec le montant crédité
        val existingProfile = bourseDao.getUserProfile()
        val updatedProfile = existingProfile?.copy(
            cashBalance = existingProfile.cashBalance + amount
        ) ?: com.example.data.local.UserEntity(
            id = 1,
            cashBalance = amount   // Premier dépôt : on initialise à ce montant
        )
        bourseDao.insertUserProfile(updatedProfile)

        // 2. Insérer la transaction de dépôt locale avec le statut TERMINÉ
        val depositTransaction = TransactionEntity(
            type = "DEPOSIT",
            title = "Dépôt $paymentMethod",
            date = dateString,
            reference = reference,
            status = "TERMINÉ",
            amount = amount
        )
        bourseDao.insertTransaction(depositTransaction)

        // 3. Synchroniser avec le serveur backend si connecté
        val currentToken = token
        if (currentToken != null) {
            try {
                com.example.data.network.ApiClient.service.submitTransaction(
                    currentToken,
                    com.example.data.network.TransactionRequest(
                        ticker = "CASH",
                        type = "DEPOSIT",
                        quantity = 1,
                        price = amount,
                        paymentRef = reference,
                        paymentMethod = paymentMethod
                    )
                )
                syncTransactions()
            } catch (e: Exception) {
                e.printStackTrace()
                // L'erreur réseau ne bloque pas : le dépôt local est déjà enregistré
            }
        }

        return true
    }


    suspend fun buyStock(
        ticker: String,
        companyName: String,
        sharesQty: Int,
        price: Double,
        feesPercent: Double = 0.005,
        sector: String
    ): String {
        if (sharesQty <= 0 || price <= 0) return "Quantité ou prix invalide."
        
        val currentToken = token
        if (currentToken != null) {
            return try {
                val ref = "OM-" + (1000..9999).random()
                val response = com.example.data.network.ApiClient.service.submitTransaction(
                    currentToken,
                    com.example.data.network.TransactionRequest(
                        ticker = ticker,
                        type = "BUY",
                        quantity = sharesQty,
                        price = price,
                        paymentRef = ref,
                        paymentMethod = "Orange Money"
                    )
                )
                if (response.isSuccessful) {
                    val buyTransaction = TransactionEntity(
                        type = "BUY",
                        title = "Achat $companyName",
                        date = "Aujourd'hui",
                        reference = ref,
                        status = "EN ATTENTE",
                        amount = -(sharesQty * price),
                        sharesQty = sharesQty,
                        stockTicker = ticker
                    )
                    bourseDao.insertTransaction(buyTransaction)
                    "SUCCESS"
                } else {
                    "Erreur API: ${response.code()}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Erreur réseau: ${e.localizedMessage}"
            }
        }

        // Fallback local
        val profile = bourseDao.getUserProfile() ?: return "Profil utilisateur introuvable."
        val totalCost = (sharesQty * price) * (1 + feesPercent)
        if (profile.cashBalance < totalCost) return "Solde insuffisant."

        val updatedProfile = profile.copy(
            cashBalance = profile.cashBalance - totalCost,
            portfolioValue = profile.portfolioValue + (sharesQty * price)
        )
        bourseDao.insertUserProfile(updatedProfile)

        val existingHolding = bourseDao.getHoldingByTicker(ticker)
        if (existingHolding != null) {
            val totalShares = existingHolding.sharesCount + sharesQty
            val newAvgPrice = ((existingHolding.sharesCount * existingHolding.averagePrice) + (sharesQty * price)) / totalShares
            bourseDao.insertHolding(existingHolding.copy(sharesCount = totalShares, averagePrice = newAvgPrice))
        } else {
            bourseDao.insertHolding(HoldingsEntity(ticker, companyName, sharesQty, price, price, 0.0, sector))
        }

        val reference = "SN-" + (1000..9999).random()
        bourseDao.insertTransaction(TransactionEntity(
            type = "BUY",
            title = "Achat $companyName",
            date = "Aujourd'hui",
            reference = reference,
            status = "TERMINÉ",
            amount = -(sharesQty * price),
            sharesQty = sharesQty,
            stockTicker = ticker
        ))
        return "SUCCESS"
    }

    suspend fun sellStock(
        ticker: String,
        sharesQty: Int,
        price: Double,
        feesPercent: Double = 0.005
    ): String {
        val existingHolding = bourseDao.getHoldingByTicker(ticker) ?: return "Vous ne possédez pas d'actions."
        if (existingHolding.sharesCount < sharesQty) return "Nombre d'actions insuffisant."

        val currentToken = token
        if (currentToken != null) {
            return try {
                val ref = "VS-" + (1000..9999).random()
                val response = com.example.data.network.ApiClient.service.submitTransaction(
                    currentToken,
                    com.example.data.network.TransactionRequest(
                        ticker = ticker,
                        type = "SELL",
                        quantity = sharesQty,
                        price = price,
                        paymentRef = ref,
                        paymentMethod = "Solde"
                    )
                )
                if (response.isSuccessful) {
                    val sellTransaction = TransactionEntity(
                        type = "SELL",
                        title = "Vente ${existingHolding.companyName}",
                        date = "Aujourd'hui",
                        reference = ref,
                        status = "EN ATTENTE",
                        amount = sharesQty * price,
                        sharesQty = sharesQty,
                        stockTicker = ticker
                    )
                    bourseDao.insertTransaction(sellTransaction)
                    "SUCCESS"
                } else {
                    "Erreur API: ${response.code()}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Erreur réseau: ${e.localizedMessage}"
            }
        }

        // Fallback local
        val profile = bourseDao.getUserProfile() ?: return "Profil introuvable."
        val totalCredit = (sharesQty * price) * (1 - feesPercent)
        bourseDao.insertUserProfile(profile.copy(cashBalance = profile.cashBalance + totalCredit, portfolioValue = profile.portfolioValue - (sharesQty * price)))

        if (existingHolding.sharesCount == sharesQty) {
            bourseDao.deleteHolding(existingHolding)
        } else {
            bourseDao.insertHolding(existingHolding.copy(sharesCount = existingHolding.sharesCount - sharesQty))
        }

        val reference = "VS-" + (1000..9999).random()
        bourseDao.insertTransaction(TransactionEntity(
            type = "SELL",
            title = "Vente ${existingHolding.companyName}",
            date = "Aujourd'hui",
            reference = reference,
            status = "TERMINÉ",
            amount = sharesQty * price,
            sharesQty = sharesQty,
            stockTicker = ticker
        ))
        return "SUCCESS"
    }

    suspend fun uploadDocument(docType: String, fileName: String, fileBase64: String): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = com.example.data.network.ApiClient.service.uploadDocument(
                currentToken,
                com.example.data.network.UploadDocumentRequest(docType, fileName, fileBase64)
            )
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun sendChatMessage(text: String): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = com.example.data.network.ApiClient.service.sendChatMessage(
                currentToken,
                com.example.data.network.ChatRequest(text)
            )
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

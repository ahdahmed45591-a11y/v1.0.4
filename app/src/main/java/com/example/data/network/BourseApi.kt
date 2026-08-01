package com.example.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val success: Boolean, val token: String, val user: UserNetworkData, val message: String?)

data class UserNetworkData(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val type: String?,
    val kyc: String?,
    val balance: Double?,
    val joinedAt: String?,
    val avatar: String?
)

data class TransactionRequest(
    val ticker: String,
    val type: String,
    val quantity: Int,
    val price: Double,
    val paymentRef: String?,
    val paymentMethod: String?
)

data class TransactionNetworkData(
    val id: String,
    val userId: String,
    val userName: String,
    val ticker: String,
    val company: String,
    val type: String,
    val quantity: Int,
    val price: Double,
    val total: Double,
    val fees: Double,
    val tva: Double,
    val grandTotal: Double,
    val status: String,
    val paymentRef: String?,
    val paymentMethod: String?,
    val rejectionReason: String?,
    val submittedAt: String
)

data class TransactionsResponse(val success: Boolean, val count: Int, val data: List<TransactionNetworkData>)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val kycStatus: String,
    val whatsapp: String? = null,
    val birthDate: String? = null,
    val profession: String? = null,
    val residence: String? = null,
    val identityDocStatus: String? = null,
    val proofOfAddressStatus: String? = null,
    val signatureStatus: String? = null
)

data class RegisterRequest(val email: String, val password: String, val firstName: String, val name: String = firstName)

data class SupportRequest(val subject: String, val message: String)

data class UploadDocumentRequest(val docType: String, val fileName: String, val fileBase64: String)

data class ChatRequest(val text: String)

data class ChatMessage(
    val id: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val sender: String,
    val text: String,
    val time: String? = null
)

interface BourseService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): retrofit2.Response<Unit>

    @POST("auth/update-profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): retrofit2.Response<Unit>

    @POST("auth/upload-document")
    suspend fun uploadDocument(
        @Header("Authorization") token: String,
        @Body request: UploadDocumentRequest
    ): retrofit2.Response<Unit>

    @GET("auth/chat")
    suspend fun getChatMessages(
        @Header("Authorization") token: String
    ): retrofit2.Response<Map<String, Any>>

    @POST("auth/chat")
    suspend fun sendChatMessage(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): retrofit2.Response<Unit>

    @POST("auth/support")
    suspend fun sendSupport(
        @Header("Authorization") token: String,
        @Body request: SupportRequest
    ): retrofit2.Response<Unit>

    @GET("transactions")
    suspend fun getTransactions(@Header("Authorization") token: String): TransactionsResponse

    @POST("transactions")
    suspend fun submitTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionRequest
    ): retrofit2.Response<Unit>
}

object ApiClient {
    private var currentUrl = "http://10.0.2.2:3001/api/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var retrofit = Retrofit.Builder()
        .baseUrl(currentUrl)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    var service: BourseService = retrofit.create(BourseService::class.java)
        private set

    fun updateBaseUrl(newUrl: String) {
        var tempUrl = newUrl.trim()
        if (!tempUrl.endsWith("/")) {
            tempUrl = "$tempUrl/"
        }
        if (!tempUrl.endsWith("api/")) {
            tempUrl = "${tempUrl}api/"
        }
        if (tempUrl == currentUrl) return
        currentUrl = tempUrl
        retrofit = Retrofit.Builder()
            .baseUrl(currentUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        service = retrofit.create(BourseService::class.java)
    }

    fun getBaseUrl(): String = currentUrl
}

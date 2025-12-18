package com.warriortech.resb.ai

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.warriortech.resb.model.MenuItem
import com.warriortech.resb.model.TblOrderDetailsResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.warriortech.resb.model.TblMenuItemResponse

@Singleton
class AIRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val aiService: AIService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AIService::class.java)
    }

    private val encryptedPrefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "ai_preferences",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun setApiKey(apiKey: String) {
        encryptedPrefs.edit { putString("openai_api_key", apiKey) }
    }

    private fun getApiKey(): String? {
        return encryptedPrefs.getString("openai_api_key", null)
    }

    suspend fun generateMenuDescription(menuItem: TblMenuItemResponse): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey =
                    getApiKey() ?: return@withContext Result.failure(Exception("API key not set"))

                val messages = listOf(
                    Message(
                        "system",
                        "You are a creative restaurant menu writer. Generate appealing descriptions for menu items."
                    ),
                    Message(
                        "user",
                        "Generate a short, appetizing description for this menu item: ${menuItem.menu_item_name}. Price: ₹${menuItem.rate}. Make it sound delicious and appealing to customers."
                    )
                )

                val request = ChatCompletionRequest(messages = messages, max_tokens = 100)
                val response = aiService.getChatCompletion("Bearer $apiKey", request)

                if (response.isSuccessful && response.body() != null) {
                    val description =
                        response.body()!!.choices.firstOrNull()?.message?.content?.trim()
                    Result.success(description ?: "Delicious item from our kitchen")
                } else {
                    Result.failure(Exception("Failed to generate description"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun suggestUpsells(orderItems: List<TblOrderDetailsResponse>): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey =
                    getApiKey() ?: return@withContext Result.failure(Exception("API key not set"))

                val itemNames = orderItems.joinToString(", ") { it.menuItem.menu_item_name }
                val messages = listOf(
                    Message(
                        "system",
                        "You are a restaurant AI assistant. Suggest complementary items to increase order value."
                    ),
                    Message(
                        "user",
                        "Based on these ordered items: $itemNames, suggest 3 complementary items that would go well with this order. Keep suggestions brief and appetizing."
                    )
                )

                val request = ChatCompletionRequest(messages = messages, max_tokens = 150)
                val response = aiService.getChatCompletion("Bearer $apiKey", request)

                if (response.isSuccessful && response.body() != null) {
                    val suggestions =
                        response.body()!!.choices.firstOrNull()?.message?.content?.trim()
                            ?.split("\n")
                            ?.filter { it.isNotBlank() }
                            ?.take(3) ?: emptyList()
                    Result.success(suggestions)
                } else {
                    Result.failure(Exception("Failed to generate suggestions"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun analyzeSalesData(orderHistory: List<TblOrderDetailsResponse>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey =
                    getApiKey() ?: return@withContext Result.failure(Exception("API key not set"))

                val salesSummary = orderHistory.groupBy { it.menuItem.menu_item_name }
                    .map { "${it.key}: ${it.value.size} orders" }
                    .take(10)
                    .joinToString(", ")

                val messages = listOf(
                    Message(
                        "system",
                        "You are a restaurant business analyst. Provide insights on sales data."
                    ),
                    Message(
                        "user",
                        "Analyze this sales data and provide 3 key insights: $salesSummary"
                    )
                )

                val request = ChatCompletionRequest(messages = messages, max_tokens = 200)
                Log.d("AIRepository", "Sending request: $request")
                val response = aiService.getChatCompletion("Bearer $apiKey", request)

                if (response.isSuccessful && response.body() != null) {
                    val analysis = response.body()!!.choices.firstOrNull()?.message?.content?.trim()
                    Result.success(analysis ?: "No insights available")
                } else {
                    Result.failure(Exception("Failed to analyze data"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun generateCustomerRecommendations(customerOrderHistory: List<TblOrderDetailsResponse>): Result<List<String>> =
        withContext(Dispatchers.IO) {
            try {
                val apiKey =
                    getApiKey() ?: return@withContext Result.failure(Exception("API key not set"))

                val favoriteItems = customerOrderHistory.groupBy { it.menuItem.menu_item_name }
                    .map { "${it.key} (${it.value.size} times)" }
                    .take(5)
                    .joinToString(", ")

                val messages = listOf(
                    Message(
                        "system",
                        "You are a restaurant recommendation engine. Suggest items based on customer preferences."
                    ),
                    Message(
                        "user",
                        "This customer frequently orders: $favoriteItems. Suggest 3 new items they might like based on their preferences."
                    )
                )

                val request = ChatCompletionRequest(messages = messages, max_tokens = 150)
                val response = aiService.getChatCompletion("Bearer $apiKey", request)

                if (response.isSuccessful && response.body() != null) {
                    val recommendations =
                        response.body()!!.choices.firstOrNull()?.message?.content?.trim()
                            ?.split("\n")
                            ?.filter { it.isNotBlank() }
                            ?.take(3) ?: emptyList()
                    Result.success(recommendations)
                } else {
                    Result.failure(Exception("Failed to generate recommendations"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

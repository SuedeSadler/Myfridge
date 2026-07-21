package com.fridgetracker.app

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String? = null,
    val category: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("date_added") val dateAdded: String? = null,
    @SerialName("estimated_expiry") val estimatedExpiry: String? = null,
    val status: String = "fresh",
    val source: String = "manual"
)

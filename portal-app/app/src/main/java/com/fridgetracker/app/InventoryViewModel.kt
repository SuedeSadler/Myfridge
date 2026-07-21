package com.fridgetracker.app

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client
    val items = mutableStateListOf<InventoryItem>()
    var isLoading = false
        private set
    var errorMessage: String? = null
        private set

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = client.postgrest["inventory_items"]
                    .select {
                        order("date_added", Order.DESCENDING)
                    }
                    .decodeList<InventoryItem>()
                items.clear()
                items.addAll(result)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load inventory"
            } finally {
                isLoading = false
            }
        }
    }

    fun addItem(name: String, quantity: Double, unit: String?, category: String?) {
        viewModelScope.launch {
            try {
                val userId = client.auth.currentSessionOrNull()?.user?.id
                if (userId == null) {
                    errorMessage = "Not signed in — please sign out and back in."
                    return@launch
                }
                val newItem = InventoryItem(
                    userId = userId,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    category = category,
                    source = "manual"
                )
                client.postgrest["inventory_items"].insert(newItem)
                refresh()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to add item"
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                client.postgrest["inventory_items"].delete {
                    filter { eq("id", id) }
                }
                items.removeAll { it.id == id }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to delete item"
            }
        }
    }

    fun updateQuantity(id: String, newQuantity: Double) {
        viewModelScope.launch {
            try {
                client.postgrest["inventory_items"].update(
                    mapOf("quantity" to newQuantity)
                ) {
                    filter { eq("id", id) }
                }
                val idx = items.indexOfFirst { it.id == id }
                if (idx >= 0) items[idx] = items[idx].copy(quantity = newQuantity)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to update quantity"
            }
        }
    }
}

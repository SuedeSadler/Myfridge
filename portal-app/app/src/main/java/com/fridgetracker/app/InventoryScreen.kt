package com.fridgetracker.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun InventoryScreen(viewModel: InventoryViewModel = viewModel()) {
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val categories = remember(viewModel.items.toList()) {
        viewModel.items.mapNotNull { it.category?.trim()?.lowercase() }.distinct().sorted()
    }

    val filteredItems = remember(viewModel.items.toList(), searchQuery, selectedCategory) {
        viewModel.items.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                item.category?.trim()?.lowercase() == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Top 64dp reserved for the system overlay; 16dp elsewhere,
                // per the Portal+ panel spec.
                .padding(
                    top = PortalSpec.topSystemPadding,
                    start = PortalSpec.screenPadding,
                    end = PortalSpec.screenPadding,
                    bottom = PortalSpec.screenPadding
                )
        ) {
            HeaderSection(itemCount = viewModel.items.size)

            Spacer(Modifier.height(PortalSpec.screenPadding))

            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })

            if (categories.isNotEmpty()) {
                Spacer(Modifier.height(PortalSpec.screenPadding))
                CategoryChipsRow(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = { selectedCategory = if (selectedCategory == it) null else it }
                )
            }

            Spacer(Modifier.height(PortalSpec.screenPadding))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    viewModel.isLoading && viewModel.items.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    filteredItems.isEmpty() -> {
                        Text(
                            if (viewModel.items.isEmpty())
                                "Nothing in the fridge yet — tap + to add an item, or scan a receipt."
                            else
                                "No items match your search.",
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.textSecondary
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 150.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredItems, key = { it.id ?: it.name }) { item ->
                                InventoryCard(
                                    item = item,
                                    onDelete = { item.id?.let { viewModel.deleteItem(it) } }
                                )
                            }
                        }
                    }
                }

                viewModel.errorMessage?.let { msg ->
                    Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                        Text(msg, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Floating add button, matches the mockup's bottom-center placement.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .size(PortalSpec.minTouchTarget + 4.dp)
                        .clip(CircleShape)
                        .background(AppColors.accent)
                        .clickable { showAddDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add item", tint = AppColors.onAccent)
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, unit, category ->
                viewModel.addItem(name, qty, unit, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HeaderSection(itemCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                "Hello",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary
            )
            Text(
                "What's in your\nfridge today?",
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "$itemCount item${if (itemCount == 1) "" else "s"} in the fridge",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.textSecondary
            )
        }
        Box(
            modifier = Modifier
                .size(PortalSpec.minTouchTarget)
                .clip(CircleShape)
                .background(AppColors.accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = AppColors.onAccent)
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = PortalSpec.minTouchTarget)
            .clip(RoundedCornerShape(999.dp))
            .background(AppColors.surface)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.textSecondary)
        Spacer(Modifier.width(10.dp))
        BasicTextFieldWithPlaceholder(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search your fridge"
        )
    }
}

@Composable
private fun BasicTextFieldWithPlaceholder(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = AppColors.textPrimary),
            modifier = Modifier.fillMaxWidth()
        )
        if (value.isEmpty()) {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.textSecondary
            )
        }
    }
}

@Composable
private fun CategoryChipsRow(
    categories: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selected
            val visual = visualFor(category)
            Box(
                modifier = Modifier
                    .heightIn(min = PortalSpec.minTouchTarget)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) AppColors.accent else AppColors.surface)
                    .clickable { onSelect(category) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        visual.monogram,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) AppColors.onAccent else AppColors.textPrimary
                    )
                    Text(
                        category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) AppColors.onAccent else AppColors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryCard(item: InventoryItem, onDelete: () -> Unit) {
    val visual = visualFor(item.category)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(visual.background)
        ) {
            if (item.photoUrl != null) {
                AsyncImage(
                    model = item.photoUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    visual.monogram,
                    style = MaterialTheme.typography.headlineSmall,
                    color = visual.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Explicit delete action — a deliberate tap, not the whole card,
            // so browsing the fridge never risks an accidental delete.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(PortalSpec.minTouchTarget)
                    .clip(CircleShape)
                    .background(AppColors.surface.copy(alpha = 0.9f))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ${item.name}",
                    tint = AppColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            item.name,
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.textPrimary,
            maxLines = 1
        )
        val qtyLabel = if (item.unit != null) "${item.quantity} ${item.unit}" else "${item.quantity}"
        Text(
            qtyLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary
        )
        item.estimatedExpiry?.let {
            Text(
                "Use by ~$it",
                style = MaterialTheme.typography.labelMedium,
                color = if (item.status == "expiring_soon") AppColors.danger else AppColors.textSecondary
            )
        }
    }
}

@Composable
private fun AddItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Double, unit: String?, category: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add item", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = PortalSpec.minTouchTarget)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = PortalSpec.minTouchTarget)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (optional)") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = PortalSpec.minTouchTarget)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (optional)") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().heightIn(min = PortalSpec.minTouchTarget)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toDoubleOrNull() ?: 1.0
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), qty, unit.ifBlank { null }, category.ifBlank { null })
                    }
                },
                modifier = Modifier.heightIn(min = PortalSpec.minTouchTarget)
            ) { Text("Add", style = MaterialTheme.typography.titleMedium) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = PortalSpec.minTouchTarget)
            ) { Text("Cancel", style = MaterialTheme.typography.titleMedium) }
        }
    )
}

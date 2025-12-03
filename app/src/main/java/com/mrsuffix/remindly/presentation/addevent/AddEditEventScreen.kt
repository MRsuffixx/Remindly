package com.mrsuffix.remindly.presentation.addevent

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsuffix.remindly.domain.model.*
import com.mrsuffix.remindly.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditEventViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Etkinliği Düzenle" else "Yeni Etkinlik",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                item {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("İsim") },
                        placeholder = { Text("Örn: Ahmet'in Doğum Günü") },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                }
                
                // Date Picker
                item {
                    DatePickerCard(
                        selectedDate = uiState.date,
                        onDateClick = { viewModel.showDatePicker(true) }
                    )
                }
                
                // Event Type Selection
                item {
                    Text(
                        text = "Etkinlik Türü",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    EventTypeSelector(
                        selectedType = uiState.eventType,
                        onTypeSelected = viewModel::updateEventType
                    )
                }
                
                // Category Selection
                item {
                    CategorySelector(
                        selectedCategory = uiState.eventCategory,
                        categories = viewModel.getCategoriesForType(uiState.eventType),
                        onCategoryClick = { viewModel.showCategoryPicker(true) }
                    )
                }
                
                // Repeat Type
                item {
                    Text(
                        text = "Tekrar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RepeatTypeSelector(
                        selectedType = uiState.repeatType,
                        onTypeSelected = viewModel::updateRepeatType
                    )
                }
                
                // Reminder Days
                item {
                    ReminderDaysSelector(
                        selectedDays = uiState.reminderDays,
                        onDayToggle = viewModel::toggleReminderDay
                    )
                }
                
                // Note
                item {
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = viewModel::updateNote,
                        label = { Text("Not (Opsiyonel)") },
                        placeholder = { Text("Hediye fikirleri, hatırlatmalar...") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                }
                
                // Save Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = viewModel::saveEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
                            )
                        } else {
                            Icon(
                                if (uiState.isEditing) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isEditing) "Güncelle" else "Kaydet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (uiState.showDatePicker) {
        DatePickerDialog(
            selectedDate = uiState.date,
            onDateSelected = viewModel::updateDate,
            onDismiss = { viewModel.showDatePicker(false) }
        )
    }
    
    // Category Picker Dialog
    if (uiState.showCategoryPicker) {
        CategoryPickerDialog(
            categories = viewModel.getCategoriesForType(uiState.eventType),
            selectedCategory = uiState.eventCategory,
            onCategorySelected = viewModel::updateEventCategory,
            onDismiss = { viewModel.showCategoryPicker(false) }
        )
    }
}

@Composable
private fun DatePickerCard(
    selectedDate: LocalDate,
    onDateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDateClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tarih",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedDate.format(
                        DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", Locale("tr"))
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EventTypeSelector(
    selectedType: EventType,
    onTypeSelected: (EventType) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(EventType.entries) { type ->
            val isSelected = type == selectedType
            val (emoji, label) = when (type) {
                EventType.BIRTHDAY -> "🎂" to "Doğum Günü"
                EventType.ANNIVERSARY -> "💍" to "Yıldönümü"
                EventType.FAMILY -> "👨‍👩‍👧" to "Aile"
                EventType.HOLIDAY -> "🎉" to "Tatil/Bayram"
                EventType.CUSTOM -> "⭐" to "Özel"
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary.copy(alpha = 0.15f),
                    selectedLabelColor = Primary
                )
            )
        }
    }
}

@Composable
private fun CategorySelector(
    selectedCategory: EventCategory,
    categories: List<EventCategory>,
    onCategoryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCategoryClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedCategory.emoji,
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = selectedCategory.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RepeatTypeSelector(
    selectedType: RepeatType,
    onTypeSelected: (RepeatType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RepeatType.entries.forEach { type ->
            val isSelected = type == selectedType
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(type) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (type == RepeatType.YEARLY) Icons.Default.Refresh else Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(type.displayName)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Secondary.copy(alpha = 0.15f),
                    selectedLabelColor = Secondary
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReminderDaysSelector(
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit
) {
    Column {
        Text(
            text = "Hatırlatma Zamanı",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val reminderOptions = listOf(
                0 to "Aynı Gün",
                1 to "1 Gün",
                3 to "3 Gün",
                7 to "1 Hafta",
                14 to "2 Hafta",
                30 to "1 Ay"
            )
            
            items(reminderOptions) { (days, label) ->
                val isSelected = selectedDays.contains(days)
                FilterChip(
                    selected = isSelected,
                    onClick = { onDayToggle(days) },
                    label = { Text(label) },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HolidayColor.copy(alpha = 0.15f),
                        selectedLabelColor = HolidayColor
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(date)
                    }
                }
            ) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<EventCategory>,
    selectedCategory: EventCategory,
    onCategorySelected: (EventCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategori Seçin") },
        text = {
            LazyColumn {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.emoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

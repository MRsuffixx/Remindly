package com.mrsuffix.remindly.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsuffix.remindly.domain.model.ThemeMode
import com.mrsuffix.remindly.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Handle import result
    LaunchedEffect(uiState.importResult) {
        uiState.importResult?.let { result ->
            when (result) {
                is ImportResult.Success -> {
                    Toast.makeText(context, "${result.count} etkinlik içe aktarıldı", Toast.LENGTH_SHORT).show()
                }
                is ImportResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.clearImportResult()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ayarlar",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Notifications Section
            item {
                SettingsSectionHeader(title = "Bildirimler", icon = Icons.Default.Notifications)
            }
            
            item {
                SettingsCard {
                    // Notification Toggle
                    SettingsToggleItem(
                        title = "Bildirimleri Etkinleştir",
                        subtitle = "Etkinlik hatırlatmaları al",
                        icon = Icons.Outlined.NotificationsActive,
                        isChecked = uiState.settings.isNotificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Notification Time
                    SettingsClickableItem(
                        title = "Bildirim Saati",
                        subtitle = uiState.settings.notificationTime.format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        ),
                        icon = Icons.Outlined.Schedule,
                        onClick = { viewModel.showTimePicker(true) }
                    )
                }
            }
            
            // Appearance Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(title = "Görünüm", icon = Icons.Default.Palette)
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = "Tema",
                        subtitle = when (uiState.themeMode) {
                            ThemeMode.SYSTEM -> "Sistem Ayarını Kullan"
                            ThemeMode.LIGHT -> "Açık Tema"
                            ThemeMode.DARK -> "Koyu Tema"
                        },
                        icon = when (uiState.themeMode) {
                            ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
                            ThemeMode.LIGHT -> Icons.Outlined.LightMode
                            ThemeMode.DARK -> Icons.Outlined.DarkMode
                        },
                        onClick = { viewModel.showThemePicker(true) }
                    )
                }
            }
            
            // Data Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(title = "Veri Yönetimi", icon = Icons.Default.Storage)
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = "Yedekle",
                        subtitle = "Etkinliklerinizi dışa aktarın",
                        icon = Icons.Outlined.CloudUpload,
                        onClick = { viewModel.exportData() }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableItem(
                        title = "Geri Yükle",
                        subtitle = "Etkinliklerinizi içe aktarın",
                        icon = Icons.Outlined.CloudDownload,
                        onClick = { viewModel.showRestoreDialog(true) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableItem(
                        title = "Bulut Senkronizasyonu",
                        subtitle = "Yakında...",
                        icon = Icons.Outlined.Cloud,
                        onClick = { },
                        enabled = false
                    )
                }
            }
            
            // About Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(title = "Hakkında", icon = Icons.Default.Info)
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = "Uygulama Hakkında",
                        subtitle = "Sürüm 1.0.0",
                        icon = Icons.Outlined.Info,
                        onClick = { viewModel.showAboutDialog(true) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Time Picker Dialog
    if (uiState.showTimePicker) {
        TimePickerDialog(
            initialTime = uiState.settings.notificationTime,
            onTimeSelected = viewModel::setNotificationTime,
            onDismiss = { viewModel.showTimePicker(false) }
        )
    }
    
    // Theme Picker Dialog
    if (uiState.showThemePicker) {
        ThemePickerDialog(
            currentMode = uiState.themeMode,
            onModeSelected = viewModel::setThemeMode,
            onDismiss = { viewModel.showThemePicker(false) }
        )
    }
    
    // Backup Dialog
    if (uiState.showBackupDialog && uiState.exportedData != null) {
        BackupDialog(
            data = uiState.exportedData!!,
            onDismiss = {
                viewModel.showBackupDialog(false)
                viewModel.clearExportedData()
            },
            context = context
        )
    }
    
    // Restore Dialog
    if (uiState.showRestoreDialog) {
        RestoreDialog(
            onImport = viewModel::importData,
            onDismiss = { viewModel.showRestoreDialog(false) }
        )
    }
    
    // About Dialog
    if (uiState.showAboutDialog) {
        AboutDialog(
            onDismiss = { viewModel.showAboutDialog(false) }
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}

@Composable
private fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) Primary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bildirim Saati") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
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
    )
}

@Composable
private fun ThemePickerDialog(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tema Seçin") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == currentMode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onModeSelected(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            when (mode) {
                                ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
                                ThemeMode.LIGHT -> Icons.Outlined.LightMode
                                ThemeMode.DARK -> Icons.Outlined.DarkMode
                            },
                            contentDescription = null,
                            tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = mode.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                        )
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

@Composable
private fun BackupDialog(
    data: String,
    onDismiss: () -> Unit,
    context: Context
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yedekleme") },
        text = {
            Column {
                Text(
                    text = "Verileriniz aşağıda gösterilmektedir. Kopyala butonuna tıklayarak güvenli bir yere kaydedin.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = data.take(500) + if (data.length > 500) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Remindly Backup", data)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Panoya kopyalandı", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kopyala")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
private fun RestoreDialog(
    onImport: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var importText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Geri Yükle") },
        text = {
            Column {
                Text(
                    text = "Daha önce yedeklediğiniz veriyi aşağıya yapıştırın.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    placeholder = { Text("JSON verisini buraya yapıştırın...") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(importText) },
                enabled = importText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("İçe Aktar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎂",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Remindly",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Primary
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sürüm 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Doğum günlerini, yıldönümlerini ve özel günleri asla unutmayın!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "❤️ ile yapıldı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

package com.mrsuffix.remindly.presentation.settings

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsuffix.remindly.R
import com.mrsuffix.remindly.data.contacts.ContactWithBirthday
import com.mrsuffix.remindly.domain.model.ThemeMode
import com.mrsuffix.remindly.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // String resources for toasts
    val contactsPermissionRequired = stringResource(R.string.contacts_permission_required)
    val eventsImportedFormat = stringResource(R.string.toast_events_imported, 0).replace("0", "%d")
    val contactsAddedFormat = stringResource(R.string.contacts_added, 0).replace("0", "%d")
    
    // Permission launcher for contacts
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showContactsDialog(true)
        } else {
            Toast.makeText(context, contactsPermissionRequired, Toast.LENGTH_SHORT).show()
        }
    }
    
    // Handle import result
    LaunchedEffect(uiState.importResult) {
        uiState.importResult?.let { result ->
            when (result) {
                is ImportResult.Success -> {
                    Toast.makeText(context, String.format(eventsImportedFormat, result.count), Toast.LENGTH_SHORT).show()
                }
                is ImportResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.clearImportResult()
        }
    }
    
    // Handle contacts import result
    LaunchedEffect(uiState.contactsImportResult) {
        uiState.contactsImportResult?.let { result ->
            when (result) {
                is ContactsImportResult.Success -> {
                    Toast.makeText(context, String.format(contactsAddedFormat, result.count), Toast.LENGTH_SHORT).show()
                }
                is ContactsImportResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.clearContactsImportResult()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                SettingsSectionHeader(title = stringResource(R.string.settings_notifications), icon = Icons.Default.Notifications)
            }
            
            item {
                SettingsCard {
                    // Notification Toggle
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_enable_notifications),
                        subtitle = stringResource(R.string.settings_notification_desc),
                        icon = Icons.Outlined.NotificationsActive,
                        isChecked = uiState.settings.isNotificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Notification Time
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_notification_time),
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
                SettingsSectionHeader(title = stringResource(R.string.settings_appearance), icon = Icons.Default.Palette)
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_theme),
                        subtitle = when (uiState.themeMode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                            ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                            ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
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
            
            // Contacts Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(title = stringResource(R.string.settings_contacts), icon = Icons.Default.Contacts)
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_import_contacts),
                        subtitle = stringResource(R.string.settings_import_contacts_desc),
                        icon = Icons.Outlined.PersonAdd,
                        iconColor = Secondary,
                        onClick = {
                            when {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.READ_CONTACTS
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    viewModel.showContactsDialog(true)
                                }
                                else -> {
                                    contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                }
                            }
                        }
                    )
                }
            }
            
            // Data Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(title = stringResource(R.string.settings_data), icon = Icons.Default.Storage)
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_backup),
                        subtitle = stringResource(R.string.settings_backup_desc),
                        icon = Icons.Outlined.CloudUpload,
                        iconColor = HolidayColor,
                        onClick = { viewModel.exportData() }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_restore),
                        subtitle = stringResource(R.string.settings_restore_desc),
                        icon = Icons.Outlined.CloudDownload,
                        iconColor = HolidayColor,
                        onClick = { viewModel.showRestoreDialog(true) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_restore_defaults),
                        subtitle = stringResource(R.string.settings_restore_defaults_desc),
                        icon = Icons.Outlined.Restore,
                        iconColor = FamilyColor,
                        onClick = { viewModel.showRestoreDefaultsDialog(true) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_delete_all),
                        subtitle = stringResource(R.string.settings_delete_all_desc),
                        icon = Icons.Outlined.DeleteForever,
                        iconColor = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.showDeleteAllDialog(true) }
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_cloud_sync),
                        subtitle = stringResource(R.string.settings_coming_soon),
                        icon = Icons.Outlined.Cloud,
                        onClick = { },
                        enabled = false
                    )
                }
            }
            
            // About Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader(title = stringResource(R.string.settings_about), icon = Icons.Default.Info)
            }
            
            item {
                SettingsCard {
                    SettingsClickableItem(
                        title = stringResource(R.string.settings_about_app),
                        subtitle = stringResource(R.string.settings_version, "1.0.0"),
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
            currentVersion = viewModel.getCurrentVersion(),
            onGitHubClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getGitHubUrl()))
                context.startActivity(intent)
            },
            onCheckUpdate = { viewModel.checkForUpdates() },
            isCheckingUpdate = uiState.isCheckingUpdate,
            onDismiss = { viewModel.showAboutDialog(false) }
        )
    }
    
    // Update Dialog
    if (uiState.showUpdateDialog && uiState.updateInfo?.hasUpdate == true) {
        UpdateDialog(
            updateInfo = uiState.updateInfo!!,
            onDownload = {
                uiState.updateInfo?.latestVersion?.downloadUrl?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
                viewModel.showUpdateDialog(false)
            },
            onDismiss = { viewModel.showUpdateDialog(false) }
        )
    }
    
    // Delete All Dialog
    if (uiState.showDeleteAllDialog) {
        DeleteAllDialog(
            onConfirm = { viewModel.deleteAllEvents() },
            onDismiss = { viewModel.showDeleteAllDialog(false) }
        )
    }
    
    // Restore Defaults Dialog
    if (uiState.showRestoreDefaultsDialog) {
        RestoreDefaultsDialog(
            onConfirm = { viewModel.restoreDefaultHolidays() },
            onDismiss = { viewModel.showRestoreDefaultsDialog(false) }
        )
    }
    
    // Contacts Dialog
    if (uiState.showContactsDialog) {
        ContactsDialog(
            contacts = uiState.contactsWithBirthdays,
            isLoading = uiState.isLoadingContacts,
            onToggleContact = viewModel::toggleContactSelection,
            onSelectAll = { viewModel.selectAllContacts(true) },
            onDeselectAll = { viewModel.selectAllContacts(false) },
            onImport = { viewModel.importSelectedContacts() },
            onDismiss = { viewModel.showContactsDialog(false) }
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
    enabled: Boolean = true,
    iconColor: Color = Primary
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
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (enabled) iconColor.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) iconColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(2.dp))
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
    currentVersion: String,
    onGitHubClick: () -> Unit,
    onCheckUpdate: () -> Unit,
    isCheckingUpdate: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon and Name
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Primary, Secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎂", fontSize = 40.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Remindly",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Primary
                )
                
                Text(
                    text = stringResource(R.string.settings_version, currentVersion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Developers Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.about_developers),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = BirthdayColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.about_developer_1),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = AnniversaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.about_developer_2),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        HorizontalDivider()
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = HolidayColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.about_university),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // GitHub Button
                    OutlinedButton(
                        onClick = onGitHubClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GitHub",
                            fontSize = 14.sp
                        )
                    }
                    
                    // Check Update Button
                    Button(
                        onClick = onCheckUpdate,
                        modifier = Modifier.weight(1f),
                        enabled = !isCheckingUpdate,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Update,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCheckingUpdate) "..." else stringResource(R.string.update_check),
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Close Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.close))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.settings_made_with_love),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UpdateDialog(
    updateInfo: com.mrsuffix.remindly.data.update.UpdateInfo,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Primary, Secondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = stringResource(R.string.update_available),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                updateInfo.latestVersion?.let { version ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.update_latest_version, version.versionName),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        if (version.isPreRelease) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFA726))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.update_pre_release),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.update_current_version, updateInfo.currentVersion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                updateInfo.latestVersion?.releaseNotes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = notes.take(200) + if (notes.length > 200) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDownload,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.update_download))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DeleteAllDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Tüm Verileri Sil",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "Tüm etkinlikleriniz kalıcı olarak silinecek. Bu işlem geri alınamaz!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Tümünü Sil")
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
private fun RestoreDefaultsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(FamilyColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Restore,
                    contentDescription = null,
                    tint = FamilyColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Varsayılan Tatilleri Yükle",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "Mevcut tatil etkinlikleri silinecek ve Türk resmi/dini tatilleri yeniden eklenecek.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = FamilyColor)
            ) {
                Text("Yükle")
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
private fun ContactsDialog(
    contacts: List<ContactWithBirthday>,
    isLoading: Boolean,
    onToggleContact: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    val selectedCount = contacts.count { it.isSelected }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Secondary, FamilyColor)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rehberden Ekle",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Kapat",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Doğum günü bilgisi olan kişiler",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
                
                // Selection controls
                if (contacts.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedCount / ${contacts.size} seçili",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row {
                            TextButton(onClick = onSelectAll) {
                                Text("Tümünü Seç")
                            }
                            TextButton(onClick = onDeselectAll) {
                                Text("Seçimi Kaldır")
                            }
                        }
                    }
                    HorizontalDivider()
                }
                
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Secondary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Rehber taranıyor...")
                                }
                            }
                        }
                        contacts.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Text(text = "📭", fontSize = 64.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Doğum günü bilgisi bulunamadı",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Rehberinizdeki kişilere doğum günü ekleyerek burada görüntüleyebilirsiniz.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(contacts, key = { it.id }) { contact ->
                                    ContactItem(
                                        contact = contact,
                                        onToggle = { onToggleContact(contact.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Footer
                if (contacts.isNotEmpty()) {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("İptal")
                        }
                        Button(
                            onClick = onImport,
                            enabled = selectedCount > 0,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ekle ($selectedCount)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: ContactWithBirthday,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (contact.isSelected) Secondary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .then(
                    if (contact.isSelected) Modifier.border(2.dp, Secondary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (contact.isSelected) Secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = contact.birthday.format(
                    DateTimeFormatter.ofPattern("d MMMM", Locale("tr"))
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Checkbox
        Checkbox(
            checked = contact.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Secondary,
                checkmarkColor = Color.White
            )
        )
    }
}

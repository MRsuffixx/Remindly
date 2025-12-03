package com.mrsuffix.remindly.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.mrsuffix.remindly.R
import com.mrsuffix.remindly.domain.model.Event
import com.mrsuffix.remindly.domain.model.EventType
import com.mrsuffix.remindly.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            HomeTopBar(
                isSearchActive = uiState.isSearchActive,
                searchQuery = uiState.searchQuery,
                onSearchActiveChange = viewModel::setSearchActive,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEvent,
                containerColor = Primary,
                contentColor = Color.White,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Etkinlik Ekle")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.isSearchActive && uiState.searchQuery.isNotBlank()) {
            SearchResultsList(
                results = uiState.searchResults,
                onEventClick = onNavigateToEditEvent,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            HomeContent(
                uiState = uiState,
                onTabSelected = viewModel::selectTab,
                onEventClick = onNavigateToEditEvent,
                onDeleteEvent = viewModel::deleteEvent,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (isSearchActive) 4.dp else 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSearchActive) {
                    // Search mode
                    IconButton(
                        onClick = { 
                            onSearchActiveChange(false)
                            onSearchQueryChange("")
                        }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.home_search_hint),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Primary)
                        )
                    }
                    
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Temizle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Normal mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(GradientStart, GradientEnd)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎂",
                                fontSize = 22.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Remindly",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                            Text(
                                text = stringResource(R.string.app_tagline),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Search button
                    IconButton(
                        onClick = { onSearchActiveChange(true) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Settings button
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onTabSelected: (HomeTab) -> Unit,
    onEventClick: (Long) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val events = when (uiState.selectedTab) {
        HomeTab.UPCOMING -> uiState.upcomingEvents
        HomeTab.THIS_WEEK -> uiState.thisWeekEvents
        HomeTab.THIS_MONTH -> uiState.thisMonthEvents
    }
    
    // Separate birthdays and special days
    val birthdays = events.filter { it.eventType == EventType.BIRTHDAY }
    val specialDays = events.filter { it.eventType != EventType.BIRTHDAY }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Quick Stats Card
        item {
            QuickStatsCard(
                todayCount = uiState.upcomingEvents.count { it.daysUntilNext() == 0 },
                thisWeekCount = uiState.thisWeekEvents.size,
                thisMonthCount = uiState.thisMonthEvents.size
            )
        }
        
        // Tab Row
        item {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = Primary,
                edgePadding = 16.dp,
                divider = {}
            ) {
                HomeTab.entries.forEach { tab ->
                    val isSelected = uiState.selectedTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Primary else Color.Transparent
                            ),
                        text = {
                            Text(
                                text = when (tab) {
                                    HomeTab.UPCOMING -> "📅 Yaklaşan"
                                    HomeTab.THIS_WEEK -> "📆 Bu Hafta"
                                    HomeTab.THIS_MONTH -> "🗓️ Bu Ay"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        if (events.isEmpty()) {
            item {
                EmptyStateView(tab = uiState.selectedTab)
            }
        } else {
            // Birthdays Section
            if (birthdays.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Doğum Günleri",
                        emoji = "🎂",
                        count = birthdays.size,
                        color = BirthdayColor
                    )
                }
                
                items(birthdays, key = { "birthday_${it.id}" }) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        onDelete = { onDeleteEvent(event.id) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            
            // Special Days Section
            if (specialDays.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Özel Günler",
                        emoji = "✨",
                        count = specialDays.size,
                        color = Secondary
                    )
                }
                
                items(specialDays, key = { "special_${it.id}" }) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        onDelete = { onDeleteEvent(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    emoji: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun QuickStatsCard(
    todayCount: Int,
    thisWeekCount: Int,
    thisMonthCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    count = todayCount,
                    label = "Bugün",
                    emoji = "🎉"
                )
                StatItem(
                    count = thisWeekCount,
                    label = "Bu Hafta",
                    emoji = "📅"
                )
                StatItem(
                    count = thisMonthCount,
                    label = "Bu Ay",
                    emoji = "🗓️"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    count: Int,
    label: String,
    emoji: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val daysUntil = event.daysUntilNext()
    val timelineColor = when {
        daysUntil == 0 -> TimelineToday
        daysUntil <= 7 -> TimelineThisWeek
        daysUntil <= 30 -> TimelineThisMonth
        else -> TimelineLater
    }
    
    val eventColor = when (event.eventType) {
        EventType.BIRTHDAY -> BirthdayColor
        EventType.ANNIVERSARY -> AnniversaryColor
        EventType.FAMILY -> FamilyColor
        EventType.HOLIDAY -> HolidayColor
        EventType.CUSTOM -> CustomColor
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            title = { 
                Text(
                    "Etkinliği Sil",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = { 
                Text(
                    "\"${event.name}\" etkinliğini silmek istediğinize emin misiniz?",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                showDeleteDialog = true
            }
            false
        }
    )
    
    Box(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.error
                                )
                            )
                        )
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sil",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            content = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timeline indicator
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(64.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(timelineColor, timelineColor.copy(alpha = 0.5f))
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.width(14.dp))
                        
                        // Emoji with gradient background
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            eventColor.copy(alpha = 0.2f),
                                            eventColor.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = eventColor.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = event.eventCategory.emoji,
                                fontSize = 26.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(14.dp))
                        
                        // Event details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(eventColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = event.eventCategory.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.nextOccurrence().format(
                                    DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE", Locale("tr"))
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Days until badge
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (daysUntil == 0) {
                                                listOf(timelineColor, timelineColor.copy(alpha = 0.8f))
                                            } else {
                                                listOf(timelineColor.copy(alpha = 0.15f), timelineColor.copy(alpha = 0.1f))
                                            }
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when {
                                        daysUntil == 0 -> "Bugün!"
                                        daysUntil == 1 -> "Yarın"
                                        else -> "$daysUntil gün"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (daysUntil == 0) Color.White else timelineColor
                                )
                            }
                            if (event.yearsSince() > 0 && event.eventType == EventType.BIRTHDAY) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${event.yearsSince() + 1}. yaş 🎈",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = eventColor
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun EmptyStateView(tab: HomeTab) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📭",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when (tab) {
                    HomeTab.UPCOMING -> "Yaklaşan etkinlik yok"
                    HomeTab.THIS_WEEK -> "Bu hafta etkinlik yok"
                    HomeTab.THIS_MONTH -> "Bu ay etkinlik yok"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Yeni bir etkinlik eklemek için + butonuna tıklayın",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: List<Event>,
    onEventClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔍", fontSize = 36.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Sonuç bulunamadı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Farklı bir arama terimi deneyin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${results.size} sonuç bulundu",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
            
            items(results, key = { "search_${it.id}" }) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event.id) },
                    onDelete = {}
                )
            }
        }
    }
}

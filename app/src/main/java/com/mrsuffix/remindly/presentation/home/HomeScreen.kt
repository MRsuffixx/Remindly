package com.mrsuffix.remindly.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    if (isSearchActive) {
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearch = {},
            active = true,
            onActiveChange = onSearchActiveChange,
            placeholder = { Text("Etkinlik ara...") },
            leadingIcon = {
                IconButton(onClick = { onSearchActiveChange(false) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                }
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Temizle")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {}
    } else {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎂",
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Remindly",
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            },
            actions = {
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Ayarlar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
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
    Column(modifier = modifier.fillMaxSize()) {
        // Quick Stats Card
        QuickStatsCard(
            todayCount = uiState.upcomingEvents.count { it.daysUntilNext() == 0 },
            thisWeekCount = uiState.thisWeekEvents.size,
            thisMonthCount = uiState.thisMonthEvents.size
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Primary
        ) {
            HomeTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                HomeTab.UPCOMING -> "Yaklaşan"
                                HomeTab.THIS_WEEK -> "Bu Hafta"
                                HomeTab.THIS_MONTH -> "Bu Ay"
                            },
                            fontWeight = if (uiState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // Events List
        val events = when (uiState.selectedTab) {
            HomeTab.UPCOMING -> uiState.upcomingEvents
            HomeTab.THIS_WEEK -> uiState.thisWeekEvents
            HomeTab.THIS_MONTH -> uiState.thisMonthEvents
        }
        
        if (events.isEmpty()) {
            EmptyStateView(tab = uiState.selectedTab)
        } else {
            EventsList(
                events = events,
                onEventClick = onEventClick,
                onDeleteEvent = onDeleteEvent
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

@Composable
private fun EventsList(
    events: List<Event>,
    onEventClick: (Long) -> Unit,
    onDeleteEvent: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events, key = { it.id }) { event ->
            EventCard(
                event = event,
                onClick = { onEventClick(event.id) },
                onDelete = { onDeleteEvent(event.id) }
            )
        }
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
            title = { Text("Etkinliği Sil") },
            text = { Text("\"${event.name}\" etkinliğini silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Sil", color = MaterialTheme.colorScheme.error)
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
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color.White
                )
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            .height(60.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(timelineColor)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Emoji
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(eventColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = event.eventCategory.emoji,
                            fontSize = 24.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Event details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.eventCategory.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = event.nextOccurrence().format(
                                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Days until badge
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(timelineColor.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                color = timelineColor
                            )
                        }
                        if (event.yearsSince() > 0 && event.eventType == EventType.BIRTHDAY) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${event.yearsSince() + 1}. yaş",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    )
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🔍", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sonuç bulunamadı",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event.id) },
                    onDelete = {}
                )
            }
        }
    }
}

package com.solace.sleep.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solace.sleep.R
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.ui.correction.CorrectionSheet
import com.solace.sleep.ui.theme.sleepDurationColor
import com.solace.sleep.util.DateFormatter
import com.solace.sleep.util.TimeExtensions.toLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onDaySelected: (LocalDate) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCorrectionSheet by remember { mutableStateOf(true) }

    if (uiState.pendingCorrectionSession != null && showCorrectionSheet) {
        CorrectionSheet(
            session = uiState.pendingCorrectionSession!!,
            onDismiss = { showCorrectionSheet = false },
            onConfirm = { showCorrectionSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) },
                actions = {
                    TextButton(onClick = viewModel::goToToday) {
                        Text(stringResource(R.string.calendar_today))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                MonthHeader(
                    yearMonth = uiState.currentMonth,
                    onPrevMonth = viewModel::goToPreviousMonth,
                    onNextMonth = viewModel::goToNextMonth
                )
                Spacer(modifier = Modifier.height(8.dp))
                WeekDayRow()
                Spacer(modifier = Modifier.height(4.dp))
                CalendarGrid(
                    yearMonth = uiState.currentMonth,
                    sessions = uiState.sessions,
                    goalMinutes = uiState.activeProfile?.sleepGoalMinutes ?: 480,
                    onDaySelected = onDaySelected
                )
                Spacer(modifier = Modifier.height(16.dp))
                SleepLegend()
            }
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            text = DateFormatter.monthYearLabel(yearMonth),
            style = MaterialTheme.typography.titleLarge
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun WeekDayRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DateFormatter.calendarWeekDayHeaders().forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    sessions: List<SleepSession>,
    goalMinutes: Int,
    onDaySelected: (LocalDate) -> Unit
) {
    val zone = ZoneId.systemDefault()
    val sessionsByDate = sessions.groupBy { it.sleepOnset.toLocalDate(zone) }
    val today = LocalDate.now()

    val firstDay = yearMonth.atDay(1)
    // Sunday = 0 offset
    val startDayOfWeek = firstDay.dayOfWeek.let {
        when (it) {
            DayOfWeek.SUNDAY -> 0
            DayOfWeek.MONDAY -> 1
            DayOfWeek.TUESDAY -> 2
            DayOfWeek.WEDNESDAY -> 3
            DayOfWeek.THURSDAY -> 4
            DayOfWeek.FRIDAY -> 5
            DayOfWeek.SATURDAY -> 6
        }
    }
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = 42 // 6 rows × 7 columns

    val cells = buildList {
        repeat(startDayOfWeek) { add(null) }
        repeat(daysInMonth) { day -> add(firstDay.plusDays(day.toLong())) }
        while (size < totalCells) add(null)
    }

    Column {
        (0 until 6).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                (0 until 7).forEach { col ->
                    val date = cells[row * 7 + col]
                    DayCell(
                        date = date,
                        daySessions = date?.let { sessionsByDate[it] } ?: emptyList(),
                        goalMinutes = goalMinutes,
                        isToday = date == today,
                        modifier = Modifier.weight(1f),
                        onClick = { date?.let { onDaySelected(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    daySessions: List<SleepSession>,
    goalMinutes: Int,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val totalMinutes = daySessions.sumOf { it.durationMinutes }
    val cellColor = if (date != null && totalMinutes > 0) {
        sleepDurationColor(totalMinutes, goalMinutes)
    } else {
        Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (date != null && totalMinutes > 0) cellColor.copy(alpha = 0.8f) else Color.Transparent)
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(if (isToday) 28.dp else 24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isToday -> MaterialTheme.colorScheme.onPrimary
                            totalMinutes > 0 -> Color.White
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                if (totalMinutes > 0 && daySessions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepLegend() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                Pair("≥100%", com.solace.sleep.ui.theme.SleepExcellent),
                Pair("80-99%", com.solace.sleep.ui.theme.SleepGood),
                Pair("60-79%", com.solace.sleep.ui.theme.SleepFair),
                Pair("<60%", com.solace.sleep.ui.theme.SleepPoor)
            ).forEach { (label, color) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

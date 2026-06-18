package com.solace.sleep.ui.correction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.solace.sleep.R
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.util.formatTime12h
import kotlinx.coroutines.launch
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CorrectionSheet(
    session: SleepSession,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: CorrectionViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val zone = ZoneId.systemDefault()

    var isFixing by remember { mutableStateOf(false) }
    var qualityScore by remember { mutableIntStateOf(0) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var notes by remember { mutableStateOf("") }

    val sleepZdt = session.sleepOnset.atZone(zone)
    val wakeZdt = session.wakeTime.atZone(zone)

    val sleepTimeState = rememberTimePickerState(
        initialHour = sleepZdt.hour,
        initialMinute = sleepZdt.minute
    )
    val wakeTimeState = rememberTimePickerState(
        initialHour = wakeZdt.hour,
        initialMinute = wakeZdt.minute
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.correction_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.correction_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            if (!isFixing) {
                // Show detected times
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.correction_bedtime_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            session.sleepOnset.formatTime12h(zone),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.correction_wake_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            session.wakeTime.formatTime12h(zone),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            } else {
                // Time pickers for correction
                Text(
                    stringResource(R.string.correction_bedtime_label),
                    style = MaterialTheme.typography.labelLarge
                )
                TimePicker(state = sleepTimeState)
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.correction_wake_label),
                    style = MaterialTheme.typography.labelLarge
                )
                TimePicker(state = wakeTimeState)
            }

            Spacer(Modifier.height(16.dp))

            // Quality rating
            Text(
                stringResource(R.string.correction_quality_label),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { qualityScore = star },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (star <= qualityScore) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star $star",
                            tint = if (star <= qualityScore) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.correction_notes_hint)) },
                maxLines = 3
            )

            Spacer(Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isFixing) {
                    OutlinedButton(
                        onClick = { isFixing = true },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.correction_fix_it)) }
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.confirmSession(
                                    session = session,
                                    qualityScore = qualityScore.takeIf { it > 0 },
                                    tags = selectedTags.toList(),
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                                onConfirm()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.correction_looks_right)) }
                } else {
                    OutlinedButton(
                        onClick = { isFixing = false },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.common_cancel)) }
                    Button(
                        onClick = {
                            scope.launch {
                                val zone2 = ZoneId.systemDefault()
                                val newSleepOnset = session.sleepOnset
                                    .atZone(zone2)
                                    .withHour(sleepTimeState.hour)
                                    .withMinute(sleepTimeState.minute)
                                    .toInstant()
                                val newWakeTime = session.wakeTime
                                    .atZone(zone2)
                                    .withHour(wakeTimeState.hour)
                                    .withMinute(wakeTimeState.minute)
                                    .toInstant()
                                viewModel.correctSession(
                                    session = session,
                                    newSleepOnset = newSleepOnset,
                                    newWakeTime = newWakeTime,
                                    qualityScore = qualityScore.takeIf { it > 0 },
                                    tags = selectedTags.toList(),
                                    notes = notes.takeIf { it.isNotBlank() }
                                )
                                onConfirm()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.correction_save)) }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

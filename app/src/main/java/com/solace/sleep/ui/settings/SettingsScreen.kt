package com.solace.sleep.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solace.sleep.BuildConfig
import com.solace.sleep.R
import com.solace.sleep.domain.model.DetectionSensitivity
import com.solace.sleep.util.minutesToHoursMinutesString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onExportClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sleep Goal
            SectionHeader(stringResource(R.string.settings_sleep_goal_section))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    profile?.let { p ->
                        var sliderValue by remember(p.sleepGoalMinutes) {
                            mutableFloatStateOf(p.sleepGoalMinutes.toFloat())
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_sleep_goal))
                            Text(
                                minutesToHoursMinutesString(sliderValue.toInt()),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = sliderValue,
                            onValueChange = { sliderValue = it },
                            onValueChangeFinished = { viewModel.updateSleepGoal(sliderValue.toInt()) },
                            valueRange = 240f..600f,
                            steps = 23 // 15-min increments between 4h and 10h
                        )
                    }
                }
            }

            // Detection
            SectionHeader(stringResource(R.string.settings_detection_section))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    profile?.let { p ->
                        Text(stringResource(R.string.settings_detection_sensitivity))
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetectionSensitivity.values().forEach { sensitivity ->
                                val isSelected = p.sensitivity == sensitivity
                                if (isSelected) {
                                    Button(onClick = {}) {
                                        Text(sensitivity.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                } else {
                                    TextButton(onClick = { viewModel.updateSensitivity(sensitivity) }) {
                                        Text(sensitivity.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sync
            SectionHeader(stringResource(R.string.settings_sync_section))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stringResource(R.string.settings_sync_google_drive))
                            Text(
                                text = if (uiState.driveSyncEnabled)
                                    stringResource(R.string.settings_sync_enabled)
                                else stringResource(R.string.settings_sync_disabled),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.driveSyncEnabled,
                            onCheckedChange = viewModel::toggleDriveSync
                        )
                    }
                }
            }

            // Export
            SectionHeader(stringResource(R.string.settings_export_section))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onExportClick,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.settings_export_data)) }
                }
            }

            // About
            SectionHeader(stringResource(R.string.settings_about_section))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

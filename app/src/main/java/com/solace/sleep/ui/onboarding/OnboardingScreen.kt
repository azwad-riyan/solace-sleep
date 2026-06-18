package com.solace.sleep.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.solace.sleep.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            label = "onboarding_step"
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (step) {
                    OnboardingStep.WELCOME -> WelcomeStep(
                        onNext = viewModel::nextStep
                    )
                    OnboardingStep.PROFILE_CREATE -> ProfileStep(
                        name = state.profileName,
                        emoji = state.profileEmoji,
                        onNameChange = viewModel::setProfileName,
                        onEmojiChange = viewModel::setProfileEmoji,
                        onNext = viewModel::nextStep,
                        onBack = viewModel::prevStep
                    )
                    OnboardingStep.PERMISSIONS -> PermissionsStep(
                        onNext = viewModel::nextStep,
                        onBack = viewModel::prevStep
                    )
                    OnboardingStep.DETECTION_WINDOW -> DetectionWindowStep(
                        onNext = viewModel::nextStep,
                        onBack = viewModel::prevStep
                    )
                    OnboardingStep.DONE -> DoneStep(
                        isLoading = state.isLoading,
                        onGetStarted = { viewModel.completeOnboarding(onOnboardingComplete) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Text("🌙", fontSize = 72.sp)
    Spacer(Modifier.height(24.dp))
    Text(
        stringResource(R.string.onboarding_welcome_title),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(R.string.onboarding_welcome_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(48.dp))
    Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.onboarding_next))
    }
}

@Composable
private fun ProfileStep(
    name: String,
    emoji: String,
    onNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Text(emoji, fontSize = 64.sp)
    Spacer(Modifier.height(16.dp))
    Text(
        stringResource(R.string.onboarding_profile_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.onboarding_profile_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.onboarding_profile_name_hint)) },
        singleLine = true
    )
    Spacer(Modifier.height(48.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.onboarding_back))
        }
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = name.isNotBlank()
        ) { Text(stringResource(R.string.onboarding_next)) }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionsStep(onNext: () -> Unit, onBack: () -> Unit) {
    val activityPermission = rememberPermissionState(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    val notifPermission = rememberPermissionState(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    Text("📱", fontSize = 64.sp)
    Spacer(Modifier.height(16.dp))
    Text(
        stringResource(R.string.onboarding_permissions_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.onboarding_permissions_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))

    if (!activityPermission.status.isGranted) {
        Button(
            onClick = { activityPermission.launchPermissionRequest() },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.permission_grant) + " Activity Recognition") }
        Spacer(Modifier.height(8.dp))
    }
    if (!notifPermission.status.isGranted) {
        Button(
            onClick = { notifPermission.launchPermissionRequest() },
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.permission_grant) + " Notifications") }
        Spacer(Modifier.height(8.dp))
    }

    Spacer(Modifier.height(24.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.onboarding_back))
        }
        Button(onClick = onNext, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.onboarding_next))
        }
    }
}

@Composable
private fun DetectionWindowStep(onNext: () -> Unit, onBack: () -> Unit) {
    Text("🌙", fontSize = 64.sp)
    Spacer(Modifier.height(16.dp))
    Text(
        stringResource(R.string.onboarding_detection_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.onboarding_detection_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(32.dp))
    // Detection window info card
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Default window", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text("9:00 PM – 10:00 AM", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(
                "You can adjust this in Settings anytime",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(48.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.onboarding_back))
        }
        Button(onClick = onNext, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.onboarding_next))
        }
    }
}

@Composable
private fun DoneStep(isLoading: Boolean, onGetStarted: () -> Unit) {
    Text("✨", fontSize = 72.sp)
    Spacer(Modifier.height(24.dp))
    Text(
        stringResource(R.string.onboarding_done_title),
        style = MaterialTheme.typography.headlineMedium
    )
    Spacer(Modifier.height(12.dp))
    Text(
        stringResource(R.string.onboarding_done_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(48.dp))
    Button(
        onClick = onGetStarted,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text(stringResource(R.string.onboarding_get_started))
        }
    }
}

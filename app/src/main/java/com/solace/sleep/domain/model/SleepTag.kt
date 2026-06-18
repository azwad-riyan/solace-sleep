package com.solace.sleep.domain.model

data class SleepTag(
    val id: String,
    val profileId: String,
    val label: String,
    val emoji: String,
    val isDefault: Boolean
)

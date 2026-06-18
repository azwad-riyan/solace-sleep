package com.solace.sleep.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable data object Onboarding : Route
    @Serializable data object Calendar : Route
    @Serializable data object Insights : Route
    @Serializable data object Profile : Route
    @Serializable data object Settings : Route
    @Serializable data object Export : Route
    @Serializable data class DayDetail(val dateEpochDay: Long) : Route
}

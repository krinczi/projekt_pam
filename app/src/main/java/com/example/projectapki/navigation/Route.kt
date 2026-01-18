package com.example.projectapki.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable data object Dashboard : Route
    @Serializable data object History : Route
    @Serializable data object Zones : Route

    @Serializable data class Detail(val id: Long) : Route
}

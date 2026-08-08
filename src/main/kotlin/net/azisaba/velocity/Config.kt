package net.azisaba.velocity

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val graphApiKey: String = "",
)

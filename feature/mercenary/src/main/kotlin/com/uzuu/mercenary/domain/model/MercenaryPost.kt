package com.uzuu.mercenary.domain.model

data class MercenaryPost(
    val id: String,
    val teamName: String,
    val captainName: String,
    val reliabilityScore: Int = 92,
    val totalMatches: Int = 34,
    val position: String, // "GK", "DF", "MF", "FW"
    val positionLabel: String,
    val matchDate: String,
    val timeSlot: String,
    val estimatedArea: String,
    val distanceKm: Double,
    val costShare: String,
    val requirements: String,
    val openSlots: Int = 1,
    val pendingApplicants: Int = 2,
    val status: String = "OPEN",
    val isApplied: Boolean = false,
)

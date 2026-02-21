package com.avinashpatil.app.dishacomputer.api.models

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardUser(
    val rank: Int,
    val score: Int,
    val userId: String,
    val dishaId: String,
    val fullName: String,
    val email: String,
    val accountNonLocked: Boolean,
    val batches: List<Batch>,
    val certificates: List<Certificate>,
    val roles: List<Role>
)
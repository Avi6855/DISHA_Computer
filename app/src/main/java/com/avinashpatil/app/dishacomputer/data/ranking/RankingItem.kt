package com.avinashpatil.app.dishacomputer.data.ranking

data class RankingItem(
    val rank: Int,
    val imageUrl: String = "https://placehold.co/50x50",
    val name: String,
    val score: Int
)
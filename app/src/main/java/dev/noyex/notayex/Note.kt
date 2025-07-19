package dev.noyex.notayex

data class Note(
    val id: Int,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)


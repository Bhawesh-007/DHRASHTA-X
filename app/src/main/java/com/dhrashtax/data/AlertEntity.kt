package com.dhrashtax.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val threatClass: String,
    val severity: String,
    val confidence: Float,
    val summary: String,
    val evidence: String,
    val detector: String = "RULE",
    val acknowledged: Boolean = false,
)


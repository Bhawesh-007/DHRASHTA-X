package com.dhrashtax.data

import com.dhrashtax.domain.RuleVerdict
import kotlinx.coroutines.flow.Flow

class AlertRepository(private val dao: AlertDao) {
    val alerts: Flow<List<AlertEntity>> = dao.observeAll()

    fun alert(id: Long): Flow<AlertEntity?> = dao.observeById(id)

    suspend fun save(verdict: RuleVerdict): Long = dao.insert(
        AlertEntity(
            timestamp = verdict.timestamp,
            packageName = verdict.packageName,
            threatClass = verdict.threatClass.name,
            severity = verdict.severity.name,
            confidence = verdict.confidence,
            summary = verdict.summary,
            evidence = verdict.evidence.joinToString("\n"),
        ),
    )

    suspend fun acknowledge(id: Long) = dao.acknowledge(id)

    suspend fun clear() = dao.clear()
}


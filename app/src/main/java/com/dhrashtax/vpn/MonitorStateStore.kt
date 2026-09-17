package com.dhrashtax.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MonitorStatus { STOPPED, STARTING, OBSERVER_ACTIVE, ERROR }

data class MonitorState(
    val status: MonitorStatus = MonitorStatus.STOPPED,
    val startedAt: Long? = null,
    val packetsObserved: Long = 0,
    val ownEgressBytes: Long = 0,
    val captureOperational: Boolean = false,
    val message: String = "Monitoring is stopped",
)

object MonitorStateStore {
    private val mutableState = MutableStateFlow(MonitorState())
    val state = mutableState.asStateFlow()

    fun update(block: (MonitorState) -> MonitorState) {
        mutableState.value = block(mutableState.value)
    }

    fun reset() {
        mutableState.value = MonitorState()
    }
}


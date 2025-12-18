package com.warriortech.resb.model

data class Counters(
    val id: Long,
    val name: String,
    val code: String, // e.g., "C1", "C2"
    val description: String = "",
    val isActive: Boolean = true,
    val location: String = "",
    val staffAssigned: String = ""
)

data class CounterSession(
    val counterId: Long,
    val counterCode: String,
    val sessionId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val totalOrders: Int = 0,
    val totalAmount: Double = 0.0,
    val isActive: Boolean = true
)

data class TblCounter(
    var counter_id: Long,
    var counter_name: String,
    var ip_address: String,
    var is_active: Boolean
)

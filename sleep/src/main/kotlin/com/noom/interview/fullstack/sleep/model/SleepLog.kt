package com.noom.interview.fullstack.sleep.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class SleepLog(
    val id: Long? = null,
    val userId: Long,
    val sleepDate: LocalDate,
    val bedTime: LocalTime,
    val wakeTime: LocalTime,
    val totalMinutes: Int,
    val feeling: Feeling,
    val createdAt: Instant? = null
)

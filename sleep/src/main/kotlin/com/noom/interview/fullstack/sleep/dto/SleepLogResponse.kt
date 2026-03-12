package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.model.Feeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import java.time.LocalDate
import java.time.LocalTime

data class SleepLogResponse(
    val sleepDate: LocalDate,
    val bedTime: LocalTime,
    val wakeTime: LocalTime,
    val totalTimeInBed: String,
    val totalMinutes: Int,
    val feeling: Feeling
)

fun SleepLog.toResponse() = SleepLogResponse(
    sleepDate = sleepDate,
    bedTime = bedTime,
    wakeTime = wakeTime,
    totalTimeInBed = formatMinutes(totalMinutes),
    totalMinutes = totalMinutes,
    feeling = feeling
)

internal fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return "${h}h ${m}min"
}

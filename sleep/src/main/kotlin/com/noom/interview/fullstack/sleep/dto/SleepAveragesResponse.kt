package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.model.Feeling
import java.time.LocalDate
import java.time.LocalTime

data class SleepAveragesResponse(
    val from: LocalDate,
    val to: LocalDate,
    val averageTotalTimeInBed: String,
    val averageTotalMinutes: Int,
    val averageBedTime: LocalTime,
    val averageWakeTime: LocalTime,
    val feelingFrequencies: Map<Feeling, Int>
)

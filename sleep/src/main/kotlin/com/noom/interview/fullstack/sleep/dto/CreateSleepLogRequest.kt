package com.noom.interview.fullstack.sleep.dto

import com.noom.interview.fullstack.sleep.model.Feeling
import java.time.LocalDate
import java.time.LocalTime
import javax.validation.constraints.NotNull

data class CreateSleepLogRequest(
    val sleepDate: LocalDate? = null,

    @field:NotNull(message = "Bed time is required")
    val bedTime: LocalTime,

    @field:NotNull(message = "Wake time is required")
    val wakeTime: LocalTime,

    @field:NotNull(message = "Feeling is required")
    val feeling: Feeling
)

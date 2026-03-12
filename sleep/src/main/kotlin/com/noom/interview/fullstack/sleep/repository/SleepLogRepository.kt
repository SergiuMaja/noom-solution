package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.SleepLog
import java.time.LocalDate

interface SleepLogRepository {
    fun insert(sleepLog: SleepLog): SleepLog
    fun findByUserIdAndDate(userId: Long, date: LocalDate): SleepLog?
    fun findByUserIdAndDateRange(userId: Long, from: LocalDate, to: LocalDate): List<SleepLog>
}

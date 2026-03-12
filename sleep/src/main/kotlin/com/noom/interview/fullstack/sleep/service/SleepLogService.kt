package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse

interface SleepLogService {
    fun createSleepLog(userId: Long, request: CreateSleepLogRequest): SleepLogResponse
    fun getLastNightSleepLog(userId: Long): SleepLogResponse
    fun getAverages(userId: Long, days: Int = 30): SleepAveragesResponse
}

package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class SleepLogController(
    private val sleepLogService: SleepLogService
) : SleepLogApi {

    override fun createSleepLog(userId: Long, request: CreateSleepLogRequest): ResponseEntity<SleepLogResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(sleepLogService.createSleepLog(userId, request))

    override fun getLastNight(userId: Long): ResponseEntity<SleepLogResponse> =
        ResponseEntity.ok(sleepLogService.getLastNightSleepLog(userId))

    override fun getAverages(userId: Long, days: Int): ResponseEntity<SleepAveragesResponse> =
        ResponseEntity.ok(sleepLogService.getAverages(userId, days))
}

package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/sleep")
class SleepLogController(
    private val sleepLogService: SleepLogService
) {

    @PostMapping
    fun createSleepLog(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateSleepLogRequest
    ): ResponseEntity<SleepLogResponse> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(sleepLogService.createSleepLog(userId, request))

    @GetMapping("/last-night")
    fun getLastNight(
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<SleepLogResponse> =
        ResponseEntity.ok(sleepLogService.getLastNightSleepLog(userId))

    @GetMapping("/averages")
    fun getAverages(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<SleepAveragesResponse> =
        ResponseEntity.ok(sleepLogService.getAverages(userId, days))
}

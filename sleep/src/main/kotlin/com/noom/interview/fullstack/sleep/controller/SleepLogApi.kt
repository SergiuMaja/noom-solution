package com.noom.interview.fullstack.sleep.controller

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.exception.GlobalExceptionHandler.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.validation.Valid

@Tag(name = "Sleep Log", description = "Sleep logging and analytics")
@RequestMapping("/api/sleep")
interface SleepLogApi {

    @Operation(summary = "Create sleep log for last night")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Sleep log created"),
        ApiResponse(responseCode = "400", description = "Validation error",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "409", description = "Sleep log already exists for today",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    )
    @PostMapping
    fun createSleepLog(
        @Parameter(description = "User identifier", required = true)
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: CreateSleepLogRequest
    ): ResponseEntity<SleepLogResponse>

    @Operation(summary = "Get last night's sleep log")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Sleep log found"),
        ApiResponse(responseCode = "404", description = "No sleep log for today",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    )
    @GetMapping("/last-night")
    fun getLastNight(
        @Parameter(description = "User identifier", required = true)
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<SleepLogResponse>

    @Operation(summary = "Get sleep averages over a period")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Averages computed"),
        ApiResponse(responseCode = "404", description = "No sleep logs in the period",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    )
    @GetMapping("/averages")
    fun getAverages(
        @Parameter(description = "User identifier", required = true)
        @RequestHeader("X-User-Id") userId: Long,
        @Parameter(description = "Number of days to average", example = "30")
        @RequestParam(defaultValue = "30") days: Int
    ): ResponseEntity<SleepAveragesResponse>
}

package com.noom.interview.fullstack.sleep.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.exception.SleepLogNotFoundException
import com.noom.interview.fullstack.sleep.model.Feeling
import com.noom.interview.fullstack.sleep.service.SleepLogService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalTime

@WebMvcTest(SleepLogController::class)
@ActiveProfiles(UNIT_TEST_PROFILE)
class SleepLogControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var sleepLogService: SleepLogService

    @Test
    fun `POST creates sleep log and returns 201`() {
        val request = CreateSleepLogRequest(
            bedTime = LocalTime.of(22, 53),
            wakeTime = LocalTime.of(7, 5),
            feeling = Feeling.GOOD
        )
        val response = SleepLogResponse(
            sleepDate = LocalDate.now(),
            bedTime = LocalTime.of(22, 53),
            wakeTime = LocalTime.of(7, 5),
            totalTimeInBed = "8h 12min",
            totalMinutes = 492,
            feeling = Feeling.GOOD
        )

        `when`(sleepLogService.createSleepLog(1L, request)).thenReturn(response)

        mockMvc.perform(
            post("/api/sleep")
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.totalMinutes").value(492))
            .andExpect(jsonPath("$.feeling").value("GOOD"))
            .andExpect(jsonPath("$.totalTimeInBed").value("8h 12min"))
    }

    @Test
    fun `POST without X-User-Id returns 400`() {
        mockMvc.perform(
            post("/api/sleep")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"bedTime":"22:00:00","wakeTime":"07:00:00","feeling":"OK"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `POST with invalid body returns 400`() {
        mockMvc.perform(
            post("/api/sleep")
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"bedTime":"not-a-time","wakeTime":"07:00:00","feeling":"OK"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET last-night returns 200`() {
        val response = SleepLogResponse(
            sleepDate = LocalDate.now(),
            bedTime = LocalTime.of(23, 0),
            wakeTime = LocalTime.of(7, 0),
            totalTimeInBed = "8h 0min",
            totalMinutes = 480,
            feeling = Feeling.OK
        )

        `when`(sleepLogService.getLastNightSleepLog(1L)).thenReturn(response)

        mockMvc.perform(
            get("/api/sleep/last-night")
                .header("X-User-Id", 1L)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalMinutes").value(480))
            .andExpect(jsonPath("$.feeling").value("OK"))
    }

    @Test
    fun `GET last-night returns 404 when not found`() {
        `when`(sleepLogService.getLastNightSleepLog(1L))
            .thenThrow(SleepLogNotFoundException("Not found"))

        mockMvc.perform(
            get("/api/sleep/last-night")
                .header("X-User-Id", 1L)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Not found"))
    }

    @Test
    fun `GET averages returns 200`() {
        val response = SleepAveragesResponse(
            from = LocalDate.of(2024, 10, 15),
            to = LocalDate.of(2024, 11, 13),
            averageTotalTimeInBed = "8h 0min",
            averageTotalMinutes = 480,
            averageBedTime = LocalTime.of(22, 30),
            averageWakeTime = LocalTime.of(6, 30),
            feelingFrequencies = mapOf(Feeling.GOOD to 20, Feeling.OK to 10)
        )

        `when`(sleepLogService.getAverages(1L, 30)).thenReturn(response)

        mockMvc.perform(
            get("/api/sleep/averages")
                .header("X-User-Id", 1L)
                .param("days", "30")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.averageTotalMinutes").value(480))
            .andExpect(jsonPath("$.averageBedTime").value("22:30:00"))
            .andExpect(jsonPath("$.averageWakeTime").value("06:30:00"))
            .andExpect(jsonPath("$.feelingFrequencies.GOOD").value(20))
    }
}

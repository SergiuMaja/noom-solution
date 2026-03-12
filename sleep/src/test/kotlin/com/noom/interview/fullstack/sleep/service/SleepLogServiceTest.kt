package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.exception.DuplicateSleepLogException
import com.noom.interview.fullstack.sleep.exception.SleepLogNotFoundException
import com.noom.interview.fullstack.sleep.model.Feeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DuplicateKeyException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class SleepLogServiceTest {

    private lateinit var repository: SleepLogRepository
    private lateinit var service: SleepLogServiceImpl

    private val fixedDate = LocalDate.of(2024, 11, 13)
    private val fixedClock = Clock.fixed(
        fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    )

    @BeforeEach
    fun setup() {
        repository = mockk()
        service = SleepLogServiceImpl(repository, fixedClock)
    }

    @Test
    fun `computeTotalMinutes handles cross-midnight`() {
        val result = SleepLogServiceImpl.computeTotalMinutes(
            LocalTime.of(22, 53), LocalTime.of(7, 5)
        )
        assertThat(result).isEqualTo(492)
    }

    @Test
    fun `computeTotalMinutes handles same-day`() {
        val result = SleepLogServiceImpl.computeTotalMinutes(
            LocalTime.of(1, 0), LocalTime.of(7, 0)
        )
        assertThat(result).isEqualTo(360)
    }

    @Test
    fun `createSleepLog computes totalMinutes and saves`() {
        val request = CreateSleepLogRequest(
            bedTime = LocalTime.of(22, 53),
            wakeTime = LocalTime.of(7, 5),
            feeling = Feeling.GOOD
        )

        val slot = slot<SleepLog>()
        every { repository.insert(capture(slot)) } answers {
            slot.captured.copy(id = 1L, createdAt = Instant.now())
        }

        val response = service.createSleepLog(1L, request)

        assertThat(response.totalMinutes).isEqualTo(492)
        assertThat(response.totalTimeInBed).isEqualTo("8h 12min")
        assertThat(response.feeling).isEqualTo(Feeling.GOOD)
        assertThat(response.sleepDate).isEqualTo(fixedDate)
    }

    @Test
    fun `createSleepLog throws DuplicateSleepLogException on duplicate`() {
        val request = CreateSleepLogRequest(
            bedTime = LocalTime.of(22, 0),
            wakeTime = LocalTime.of(7, 0),
            feeling = Feeling.OK
        )

        every { repository.insert(any()) } throws DuplicateKeyException("duplicate")

        assertThrows<DuplicateSleepLogException> {
            service.createSleepLog(1L, request)
        }
    }

    @Test
    fun `getLastNightSleepLog returns response when found`() {
        val sleepLog = SleepLog(
            id = 1L,
            userId = 1L,
            sleepDate = fixedDate,
            bedTime = LocalTime.of(23, 0),
            wakeTime = LocalTime.of(7, 0),
            totalMinutes = 480,
            feeling = Feeling.OK
        )

        every { repository.findByUserIdAndDate(1L, fixedDate) } returns sleepLog

        val response = service.getLastNightSleepLog(1L)

        assertThat(response.totalMinutes).isEqualTo(480)
        assertThat(response.feeling).isEqualTo(Feeling.OK)
    }

    @Test
    fun `createSleepLog uses provided sleepDate instead of today`() {
        val customDate = LocalDate.of(2024, 11, 10)
        val request = CreateSleepLogRequest(
            sleepDate = customDate,
            bedTime = LocalTime.of(23, 0),
            wakeTime = LocalTime.of(7, 0),
            feeling = Feeling.OK
        )

        val slot = slot<SleepLog>()
        every { repository.insert(capture(slot)) } answers {
            slot.captured.copy(id = 1L, createdAt = Instant.now())
        }

        val response = service.createSleepLog(1L, request)

        assertThat(response.sleepDate).isEqualTo(customDate)
    }

    @Test
    fun `getLastNightSleepLog throws when not found`() {
        every { repository.findByUserIdAndDate(1L, fixedDate) } returns null

        assertThrows<SleepLogNotFoundException> {
            service.getLastNightSleepLog(1L)
        }
    }

    @Test
    fun `getAverages returns correct averages`() {
        val logs = listOf(
            SleepLog(id = 1, userId = 1, sleepDate = fixedDate, bedTime = LocalTime.of(22, 0), wakeTime = LocalTime.of(6, 0), totalMinutes = 480, feeling = Feeling.GOOD),
            SleepLog(id = 2, userId = 1, sleepDate = fixedDate.minusDays(1), bedTime = LocalTime.of(23, 0), wakeTime = LocalTime.of(7, 0), totalMinutes = 480, feeling = Feeling.OK),
            SleepLog(id = 3, userId = 1, sleepDate = fixedDate.minusDays(2), bedTime = LocalTime.of(22, 30), wakeTime = LocalTime.of(7, 30), totalMinutes = 540, feeling = Feeling.GOOD)
        )

        every { repository.findByUserIdAndDateRange(1L, any(), any()) } returns logs

        val result = service.getAverages(1L, 30)

        assertThat(result.averageTotalMinutes).isEqualTo(500)
        assertThat(result.averageTotalTimeInBed).isEqualTo("8h 20min")
        assertThat(result.feelingFrequencies[Feeling.GOOD]).isEqualTo(2)
        assertThat(result.feelingFrequencies[Feeling.OK]).isEqualTo(1)
        assertThat(result.from).isEqualTo(fixedDate.minusDays(29))
        assertThat(result.to).isEqualTo(fixedDate)
    }

    @Test
    fun `getAverages throws when no logs found`() {
        every { repository.findByUserIdAndDateRange(1L, any(), any()) } returns emptyList()

        assertThrows<SleepLogNotFoundException> {
            service.getAverages(1L, 30)
        }
    }

    @Test
    fun `averageTime with noon offset handles cross-midnight bed times`() {
        val times = listOf(LocalTime.of(23, 0), LocalTime.of(1, 0))
        val result = SleepLogServiceImpl.averageTime(times, useNoonOffset = true)
        assertThat(result).isEqualTo(LocalTime.of(0, 0))
    }

    @Test
    fun `averageTime without noon offset averages wake times`() {
        val times = listOf(LocalTime.of(6, 0), LocalTime.of(8, 0))
        val result = SleepLogServiceImpl.averageTime(times, useNoonOffset = false)
        assertThat(result).isEqualTo(LocalTime.of(7, 0))
    }
}

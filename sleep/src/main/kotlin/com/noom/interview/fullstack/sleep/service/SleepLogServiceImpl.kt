package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepAveragesResponse
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.dto.formatMinutes
import com.noom.interview.fullstack.sleep.dto.toResponse
import com.noom.interview.fullstack.sleep.exception.DuplicateSleepLogException
import com.noom.interview.fullstack.sleep.exception.SleepLogNotFoundException
import com.noom.interview.fullstack.sleep.model.Feeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import com.noom.interview.fullstack.sleep.repository.SleepLogRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

@Service
class SleepLogServiceImpl(
    private val repository: SleepLogRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) : SleepLogService {

    private fun today(): LocalDate = LocalDate.now(clock)

    override fun createSleepLog(userId: Long, request: CreateSleepLogRequest): SleepLogResponse {
        val totalMinutes = computeTotalMinutes(request.bedTime, request.wakeTime)

        val sleepLog = SleepLog(
            userId = userId,
            sleepDate = today(),
            bedTime = request.bedTime,
            wakeTime = request.wakeTime,
            totalMinutes = totalMinutes,
            feeling = request.feeling
        )

        val saved = try {
            repository.insert(sleepLog)
        } catch (ex: DuplicateKeyException) {
            throw DuplicateSleepLogException("Sleep log already exists for today")
        }

        return saved.toResponse()
    }

    override fun getLastNightSleepLog(userId: Long): SleepLogResponse {
        val sleepLog = repository.findByUserIdAndDate(userId, today())
            ?: throw SleepLogNotFoundException("No sleep log found for today")

        return sleepLog.toResponse()
    }

    override fun getAverages(userId: Long, days: Int): SleepAveragesResponse {
        val to = today()
        val from = to.minusDays(days.toLong() - 1)

        val logs = repository.findByUserIdAndDateRange(userId, from, to)
        if (logs.isEmpty()) {
            throw SleepLogNotFoundException("No sleep logs found for the last $days days")
        }

        val avgTotalMinutes = logs.map { it.totalMinutes }.average().toInt()
        val avgBedTime = averageTime(logs.map { it.bedTime }, useNoonOffset = true)
        val avgWakeTime = averageTime(logs.map { it.wakeTime }, useNoonOffset = false)

        val feelingFrequencies = Feeling.values().associateWith { feeling ->
            logs.count { it.feeling == feeling }
        }

        return SleepAveragesResponse(
            from = from,
            to = to,
            averageTotalTimeInBed = formatMinutes(avgTotalMinutes),
            averageTotalMinutes = avgTotalMinutes,
            averageBedTime = avgBedTime,
            averageWakeTime = avgWakeTime,
            feelingFrequencies = feelingFrequencies
        )
    }

    companion object {
        fun computeTotalMinutes(bedTime: LocalTime, wakeTime: LocalTime): Int {
            var duration = Duration.between(bedTime, wakeTime)
            if (duration.isNegative) {
                duration = duration.plusHours(24)
            }
            return duration.toMinutes().toInt()
        }

        fun averageTime(times: List<LocalTime>, useNoonOffset: Boolean): LocalTime {
            if (times.isEmpty()) return LocalTime.MIDNIGHT

            val avgMinutes = if (useNoonOffset) {
                val minutesFromNoon = times.map { time ->
                    val minutesOfDay = time.hour * 60 + time.minute
                    (minutesOfDay - 720 + 1440) % 1440
                }
                val avg = minutesFromNoon.average().toInt()
                (avg + 720) % 1440
            } else {
                times.map { it.hour * 60 + it.minute }.average().toInt()
            }

            return LocalTime.of(avgMinutes / 60, avgMinutes % 60)
        }
    }
}

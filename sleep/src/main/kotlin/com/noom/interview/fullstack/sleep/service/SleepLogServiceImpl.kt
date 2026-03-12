package com.noom.interview.fullstack.sleep.service

import com.noom.interview.fullstack.sleep.dto.CreateSleepLogRequest
import com.noom.interview.fullstack.sleep.dto.SleepLogResponse
import com.noom.interview.fullstack.sleep.dto.toResponse
import com.noom.interview.fullstack.sleep.exception.DuplicateSleepLogException
import com.noom.interview.fullstack.sleep.exception.SleepLogNotFoundException
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

    companion object {
        fun computeTotalMinutes(bedTime: LocalTime, wakeTime: LocalTime): Int {
            var duration = Duration.between(bedTime, wakeTime)
            if (duration.isNegative) {
                duration = duration.plusHours(24)
            }
            return duration.toMinutes().toInt()
        }
    }
}

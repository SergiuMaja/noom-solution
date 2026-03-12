package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.SleepApplication.Companion.UNIT_TEST_PROFILE
import com.noom.interview.fullstack.sleep.model.Feeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@ActiveProfiles(UNIT_TEST_PROFILE)
class SleepLogRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    private lateinit var repository: JdbcSleepLogRepository

    @BeforeEach
    fun setup() {
        repository = JdbcSleepLogRepository(jdbcTemplate)
        jdbcTemplate.jdbcTemplate.execute("DELETE FROM sleep_log")
    }

    @Test
    fun `insert and retrieve sleep log`() {
        val sleepLog = createSleepLog(userId = 1, date = LocalDate.now())

        val saved = repository.insert(sleepLog)

        assertThat(saved.id).isNotNull()
        assertThat(saved.userId).isEqualTo(1L)
        assertThat(saved.sleepDate).isEqualTo(LocalDate.now())
        assertThat(saved.bedTime).isEqualTo(LocalTime.of(22, 53))
        assertThat(saved.wakeTime).isEqualTo(LocalTime.of(7, 5))
        assertThat(saved.totalMinutes).isEqualTo(492)
        assertThat(saved.feeling).isEqualTo(Feeling.GOOD)
    }

    @Test
    fun `find by user id and date`() {
        val sleepLog = createSleepLog(userId = 1, date = LocalDate.now())
        repository.insert(sleepLog)

        val found = repository.findByUserIdAndDate(1L, LocalDate.now())

        assertThat(found).isNotNull
        assertThat(found!!.userId).isEqualTo(1L)
        assertThat(found.sleepDate).isEqualTo(LocalDate.now())
    }

    @Test
    fun `find by user id and date returns null when not found`() {
        val found = repository.findByUserIdAndDate(999L, LocalDate.now())

        assertThat(found).isNull()
    }

    @Test
    fun `duplicate insert throws exception`() {
        val sleepLog = createSleepLog(userId = 1, date = LocalDate.now())
        repository.insert(sleepLog)

        assertThrows<DuplicateKeyException> {
            repository.insert(sleepLog)
        }
    }

    @Test
    fun `find by user id and date range`() {
        val today = LocalDate.now()
        repository.insert(createSleepLog(userId = 1, date = today))
        repository.insert(createSleepLog(userId = 1, date = today.minusDays(1)))
        repository.insert(createSleepLog(userId = 1, date = today.minusDays(5)))
        // Outside range
        repository.insert(createSleepLog(userId = 1, date = today.minusDays(31)))
        // Different user
        repository.insert(createSleepLog(userId = 2, date = today))

        val logs = repository.findByUserIdAndDateRange(1L, today.minusDays(29), today)

        assertThat(logs).hasSize(3)
        assertThat(logs.first().sleepDate).isEqualTo(today)
        assertThat(logs.last().sleepDate).isEqualTo(today.minusDays(5))
    }

    private fun createSleepLog(userId: Long, date: LocalDate) = SleepLog(
        userId = userId,
        sleepDate = date,
        bedTime = LocalTime.of(22, 53),
        wakeTime = LocalTime.of(7, 5),
        totalMinutes = 492,
        feeling = Feeling.GOOD
    )
}

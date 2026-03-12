package com.noom.interview.fullstack.sleep.repository

import com.noom.interview.fullstack.sleep.model.Feeling
import com.noom.interview.fullstack.sleep.model.SleepLog
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDate

@Repository
class JdbcSleepLogRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : SleepLogRepository {

    private val rowMapper = RowMapper { rs: ResultSet, _: Int ->
        SleepLog(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            sleepDate = rs.getDate("sleep_date").toLocalDate(),
            bedTime = rs.getTime("bed_time").toLocalTime(),
            wakeTime = rs.getTime("wake_time").toLocalTime(),
            totalMinutes = rs.getInt("total_minutes"),
            feeling = Feeling.valueOf(rs.getString("feeling")),
            createdAt = rs.getTimestamp("created_at").toInstant()
        )
    }

    override fun insert(sleepLog: SleepLog): SleepLog {
        val sql = """
            INSERT INTO sleep_log (user_id, sleep_date, bed_time, wake_time, total_minutes, feeling)
            VALUES (:userId, :sleepDate, :bedTime, :wakeTime, :totalMinutes, :feeling)
            RETURNING *
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("userId", sleepLog.userId)
            .addValue("sleepDate", sleepLog.sleepDate)
            .addValue("bedTime", sleepLog.bedTime)
            .addValue("wakeTime", sleepLog.wakeTime)
            .addValue("totalMinutes", sleepLog.totalMinutes)
            .addValue("feeling", sleepLog.feeling.name)

        return jdbcTemplate.queryForObject(sql, params, rowMapper)!!
    }

    override fun findByUserIdAndDate(userId: Long, date: LocalDate): SleepLog? {
        val sql = """
            SELECT * FROM sleep_log
            WHERE user_id = :userId AND sleep_date = :date
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("date", date)

        return jdbcTemplate.query(sql, params, rowMapper).firstOrNull()
    }

    override fun findByUserIdAndDateRange(userId: Long, from: LocalDate, to: LocalDate): List<SleepLog> {
        val sql = """
            SELECT * FROM sleep_log
            WHERE user_id = :userId AND sleep_date BETWEEN :from AND :to
            ORDER BY sleep_date DESC
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("from", from)
            .addValue("to", to)

        return jdbcTemplate.query(sql, params, rowMapper)
    }
}

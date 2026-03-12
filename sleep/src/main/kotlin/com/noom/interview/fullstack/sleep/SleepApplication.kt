package com.noom.interview.fullstack.sleep

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication
class SleepApplication {
	companion object {
		const val UNIT_TEST_PROFILE = "unittest"
	}

	@Bean
	fun clock(): Clock = Clock.systemDefaultZone()
}

fun main(args: Array<String>) {
	runApplication<SleepApplication>(*args)
}

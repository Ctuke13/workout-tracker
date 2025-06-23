
package com.chidituke.workout_tracker;

import com.chidituke.workout_tracker.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Integration test that verifies the Spring Boot application context loads successfully.
 * Uses TestContainers for real database testing.
 */
class WorkoutTrackerApplicationTests extends BaseIntegrationTest {

	@Test
	void contextLoads() {
		// This test passes if the Spring Boot application context loads successfully
		// with TestContainers PostgreSQL database

		// Optional: Add some basic verification
		System.out.println("✅ Spring Boot application context loaded successfully!");
		System.out.println("✅ Database URL: " + getDatabaseUrl());
		System.out.println("✅ Database running: " + isDatabaseRunning());
	}
}
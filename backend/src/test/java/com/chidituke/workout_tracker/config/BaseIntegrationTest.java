// =============================================================================
// BASE INTEGRATION TEST - Place in src/test/java/.../config/
// =============================================================================

package com.chidituke.workout_tracker.config;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base test configuration that sets up TestContainers PostgreSQL database.
 * Extend this class for integration tests that need a real database.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("workout_tracker_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Force PostgreSQL configuration
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // JPA/Hibernate settings
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        // Flyway settings
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);
    }

    @BeforeAll
    static void beforeAll() {
        // Ensure container starts before tests
        if (!postgresContainer.isRunning()) {
            postgresContainer.start();
        }
    }

    /**
     * Helper method to get the actual database URL for debugging
     */
    protected String getDatabaseUrl() {
        return postgresContainer.getJdbcUrl();
    }

    /**
     * Helper method to verify container is running
     */
    protected boolean isDatabaseRunning() {
        return postgresContainer.isRunning();
    }
}
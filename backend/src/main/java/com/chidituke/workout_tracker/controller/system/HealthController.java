package com.chidituke.workout_tracker.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Controller - Provides application health and status information
 * This endpoint is used by:
 * - Load balancers for health checks
 * - Monitoring tools for service status
 * - DevOps teams for deployment verification
 * - Frontend for API connectivity testing
 */
@RestController
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React development
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false) // Optional - may not be available in all profiles
    private BuildProperties buildProperties;

    /**
     * Basic health check endpoint
     * Returns simple UP/DOWN status with timestamp
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Basic application health
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("service", "workout-tracker");
            health.put("environment", System.getProperty("spring.profiles.active", "development"));

            // Add build information if available
            if (buildProperties != null) {
                health.put("version", buildProperties.getVersion());
                health.put("buildTime", buildProperties.getTime());
            } else {
                health.put("version", "development");
            }

            // Check database connectivity
            Map<String, Object> database = checkDatabaseHealth();
            health.put("database", database);

            // Add system information
            Map<String, Object> system = getSystemInfo();
            health.put("system", system);

            // Overall health status
            boolean isHealthy = "UP".equals(database.get("status"));
            health.put("status", isHealthy ? "UP" : "DOWN");

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            // If anything fails, return DOWN status
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(503).body(health); // 503 Service Unavailable
        }
    }

    /**
     * Detailed health check with dependency status
     * Use this for more comprehensive monitoring
     */
    @GetMapping("/api/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            health.put("service", "workout-tracker");
            health.put("timestamp", LocalDateTime.now());

            // Check all dependencies
            Map<String, Object> dependencies = new HashMap<>();
            dependencies.put("database", checkDatabaseHealth());
            // Add more dependencies here as your app grows
            // dependencies.put("redis", checkRedisHealth());
            // dependencies.put("externalApi", checkExternalApiHealth());

            health.put("dependencies", dependencies);

            // System metrics
            health.put("system", getDetailedSystemInfo());

            // Application info
            Map<String, Object> application = new HashMap<>();
            application.put("name", "Workout Tracker API");
            application.put("description", "Backend API for workout and exercise tracking");

            if (buildProperties != null) {
                application.put("version", buildProperties.getVersion());
                application.put("buildTime", buildProperties.getTime());
                application.put("artifact", buildProperties.getArtifact());
                application.put("group", buildProperties.getGroup());
            } else {
                application.put("version", "development");
                application.put("profile", "local-development");
            }

            health.put("application", application);

            // Determine overall status
            boolean isHealthy = dependencies.values().stream()
                    .allMatch(dep -> "UP".equals(((Map<?, ?>) dep).get("status")));

            health.put("status", isHealthy ? "UP" : "DOWN");

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Simple ping endpoint for basic connectivity test
     */
    @GetMapping("/api/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "workout-tracker");

        return ResponseEntity.ok(response);
    }

    /**
     * Check database connectivity and basic metrics
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            // Test database connectivity
            boolean isValid = connection.isValid(5); // 5 second timeout

            dbHealth.put("status", isValid ? "UP" : "DOWN");
            dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
            dbHealth.put("version", connection.getMetaData().getDatabaseProductVersion());
            dbHealth.put("url", connection.getMetaData().getURL());
            dbHealth.put("driver", connection.getMetaData().getDriverName());

            // Test a simple query
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT COUNT(*) as exercise_count FROM exercises")) {

                if (resultSet.next()) {
                    dbHealth.put("exerciseCount", resultSet.getInt("exercise_count"));
                }
            }

        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }

        return dbHealth;
    }

    /**
     * Get basic system information
     */
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();

        system.put("javaVersion", System.getProperty("java.version"));
        system.put("maxMemory", formatBytes(runtime.maxMemory()));
        system.put("totalMemory", formatBytes(runtime.totalMemory()));
        system.put("freeMemory", formatBytes(runtime.freeMemory()));
        system.put("availableProcessors", runtime.availableProcessors());

        return system;
    }

    /**
     * Get detailed system information for comprehensive monitoring
     */
    private Map<String, Object> getDetailedSystemInfo() {
        Map<String, Object> system = getSystemInfo();

        // Add more detailed system info
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("osArch", System.getProperty("os.arch"));
        system.put("javaVendor", System.getProperty("java.vendor"));
        system.put("workingDirectory", System.getProperty("user.dir"));

        // JVM uptime
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        system.put("uptimeMs", uptimeMs);
        system.put("uptime", formatDuration(uptimeMs));

        return system;
    }

    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Format duration to human-readable format
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        if (hours > 0) return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds % 60);
        return String.format("%ds", seconds);
    }
}
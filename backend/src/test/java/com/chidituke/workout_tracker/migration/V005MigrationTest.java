// =============================================================================
// V005 MIGRATION TEST - Place in src/test/java/.../migration/
// =============================================================================

package com.chidituke.workout_tracker.migration;

import com.chidituke.workout_tracker.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that validate the complete Workout Tracking System created across V005 and V006 migrations.
 * - V005: Creates tables (workout_sessions, performance_records)
 * - V006: Creates triggers for updated_at timestamps
 * This test validates that the migrations exactly match the WorkoutSession and PerformanceRecord entities.
 */
class V005MigrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateWorkoutSessionsTableWithAllColumns() {
        // UPDATED: Verify workout_sessions table exists (changed from workout_logs)
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'workout_sessions')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        // Verify all WorkoutSession entity columns exist
        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'workout_sessions'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // UPDATED: Core fields - changed to workout_session_id and workout_plan_id
        assertThat(columnNames).contains("workout_session_id", "user_id", "workout_plan_id");

        // Session metrics
        assertThat(columnNames).contains("total_duration_minutes", "estimated_calories",
                "difficulty_rating", "overall_effort");

        // Session context
        assertThat(columnNames).contains("mood", "location");

        // UPDATED: Program integration - changed program_id to workout_program_id
        assertThat(columnNames).contains("workout_program_id", "week_number", "scheduled_workout_id");

        // Social features
        assertThat(columnNames).contains("is_shared");

        // Core data
        assertThat(columnNames).contains("date", "notes");

        // Timestamps
        assertThat(columnNames).contains("created_at", "updated_at");
    }

    @Test
    void shouldCreatePerformanceRecordsTableWithAllColumns() {
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'performance_records')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'performance_records'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // UPDATED: Core relationships - changed to performance_record_id and workout_session_id
        assertThat(columnNames).contains("performance_record_id", "exercise_id", "workout_session_id");

        // Basic performance metrics
        assertThat(columnNames).contains("set_number", "reps", "weight");

        // Cardio metrics
        assertThat(columnNames).contains("duration_minutes", "duration_seconds", "distance_km", "calories_burned");

        // Advanced performance metrics
        assertThat(columnNames).contains("perceived_exertion", "form_rating", "rest_seconds", "tempo");

        // Specialized exercise metrics
        assertThat(columnNames).contains("hold_duration_seconds", "balance_score", "jump_height_cm", "power_output_watts");

        // Professional training metrics
        assertThat(columnNames).contains("assigned_by_trainer_id", "target_reps", "target_weight", "achievement_status");

        // Notes and metadata
        assertThat(columnNames).contains("notes", "equipment_used", "workout_environment");

        // Audit fields
        assertThat(columnNames).contains("created_at", "updated_at");
    }

    @Test
    void shouldCreateForeignKeyConstraints() {
        // Verify foreign key relationships exist
        var constraints = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, tc.table_name, kcu.column_name, ccu.table_name AS foreign_table_name
                FROM information_schema.table_constraints AS tc 
                JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'FOREIGN KEY' 
                AND tc.table_name IN ('workout_sessions', 'performance_records')
                """
        );

        assertThat(constraints).hasSizeGreaterThan(0);

        // UPDATED: Verify workout_sessions.user_id references users(user_id)
        assertThat(constraints).anyMatch(row ->
                "workout_sessions".equals(row.get("table_name")) &&
                        "user_id".equals(row.get("column_name")) &&
                        "users".equals(row.get("foreign_table_name"))
        );

        // UPDATED: Verify workout_sessions.workout_plan_id references workout_plans
        assertThat(constraints).anyMatch(row ->
                "workout_sessions".equals(row.get("table_name")) &&
                        "workout_plan_id".equals(row.get("column_name")) &&
                        "workout_plans".equals(row.get("foreign_table_name"))
        );

        // Verify performance_records.exercise_id references exercises
        assertThat(constraints).anyMatch(row ->
                "performance_records".equals(row.get("table_name")) &&
                        "exercise_id".equals(row.get("column_name")) &&
                        "exercises".equals(row.get("foreign_table_name"))
        );

        // UPDATED: Verify performance_records.workout_session_id references workout_sessions
        assertThat(constraints).anyMatch(row ->
                "performance_records".equals(row.get("table_name")) &&
                        "workout_session_id".equals(row.get("column_name")) &&
                        "workout_sessions".equals(row.get("foreign_table_name"))
        );
    }

    @Test
    void shouldCreateRequiredIndexes() {
        // UPDATED: Test that critical indexes exist for performance (changed table name)
        var indexes = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename IN ('workout_sessions', 'performance_records')"
        );
        var indexNames = indexes.stream()
                .map(row -> row.get("indexname").toString())
                .toList();

        // UPDATED: Check critical indexes exist for workout_sessions (changed column names)
        assertThat(indexNames).anyMatch(name -> name.contains("user_id"));
        assertThat(indexNames).anyMatch(name -> name.contains("workout_plan_id"));
        assertThat(indexNames).anyMatch(name -> name.contains("date"));

        // UPDATED: Check critical indexes exist for performance_records (changed to workout_session_id)
        assertThat(indexNames).anyMatch(name -> name.contains("performance_workout_session") || name.contains("workout_session_id"));
        assertThat(indexNames).anyMatch(name -> name.contains("performance_exercise") || name.contains("exercise_id"));
        assertThat(indexNames).anyMatch(name -> name.contains("performance_user_date"));
        assertThat(indexNames).anyMatch(name -> name.contains("performance_user_exercise"));
        assertThat(indexNames).anyMatch(name -> name.contains("performance_set_number"));
    }

    @Test
    void shouldCreateCheckConstraints() {
        // UPDATED: Test that validation constraints exist for workout_sessions (changed table name)
        var workoutSessionsConstraints = jdbcTemplate.queryForList(
                """
                SELECT constraint_name, check_clause 
                FROM information_schema.check_constraints 
                WHERE constraint_name LIKE 'chk_workout_sessions_%'
                """
        );

        assertThat(workoutSessionsConstraints).hasSizeGreaterThan(0);

        var workoutConstraintNames = workoutSessionsConstraints.stream()
                .map(row -> row.get("constraint_name").toString())
                .toList();

        // UPDATED: Changed constraint names to match new table name
        assertThat(workoutConstraintNames).contains("chk_workout_sessions_difficulty_rating");
        assertThat(workoutConstraintNames).contains("chk_workout_sessions_overall_effort");
        assertThat(workoutConstraintNames).contains("chk_workout_sessions_mood");
        assertThat(workoutConstraintNames).contains("chk_workout_sessions_location");

        // Test that validation constraints exist for performance_records
        var performanceConstraints = jdbcTemplate.queryForList(
                """
                SELECT constraint_name 
                FROM information_schema.check_constraints 
                WHERE constraint_name LIKE 'chk_performance_records_%'
                """
        );

        assertThat(performanceConstraints).hasSizeGreaterThan(0);

        var performanceConstraintNames = performanceConstraints.stream()
                .map(row -> row.get("constraint_name").toString())
                .toList();

        assertThat(performanceConstraintNames).contains("chk_performance_records_set_number");
        assertThat(performanceConstraintNames).contains("chk_performance_records_perceived_exertion");
        assertThat(performanceConstraintNames).contains("chk_performance_records_form_rating");
        assertThat(performanceConstraintNames).contains("chk_performance_records_achievement_status");
    }

    @Test
    void shouldCreateUniqueConstraints() {
        // Test unique constraints
        var uniqueConstraints = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, tc.table_name, ccu.column_name
                FROM information_schema.constraint_column_usage ccu
                JOIN information_schema.table_constraints tc ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'UNIQUE'
                AND tc.table_name IN ('workout_sessions', 'performance_records')
                """
        );

        // Check for unique indexes (since we created UNIQUE INDEX, not UNIQUE CONSTRAINT)
        var uniqueIndexes = jdbcTemplate.queryForList(
                """
                SELECT indexname, tablename 
                FROM pg_indexes 
                WHERE tablename IN ('workout_sessions', 'performance_records')
                AND indexname LIKE 'idx_unique_%'
                """
        );

        assertThat(uniqueIndexes).hasSizeGreaterThan(0);

        // UPDATED: Verify specific unique constraints exist (changed table name)
        assertThat(uniqueIndexes).anyMatch(row ->
                "workout_sessions".equals(row.get("tablename")) &&
                        "idx_unique_scheduled_workout_session".equals(row.get("indexname"))
        );

        assertThat(uniqueIndexes).anyMatch(row ->
                "performance_records".equals(row.get("tablename")) &&
                        "idx_unique_performance_set".equals(row.get("indexname"))
        );
    }

    @Test
    void shouldHaveCorrectDataTypes() {
        // UPDATED: Test specific data types for critical columns (changed table name)
        var workoutSessionsColumns = jdbcTemplate.queryForList(
                """
                SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale
                FROM information_schema.columns 
                WHERE table_name = 'workout_sessions'
                AND column_name IN ('difficulty_rating', 'overall_effort', 'mood', 'date')
                """
        );

        // Verify difficulty_rating is INTEGER
        assertThat(workoutSessionsColumns).anyMatch(row ->
                "difficulty_rating".equals(row.get("column_name")) &&
                        "integer".equals(row.get("data_type"))
        );

        // Verify overall_effort is DOUBLE PRECISION
        assertThat(workoutSessionsColumns).anyMatch(row ->
                "overall_effort".equals(row.get("column_name")) &&
                        "double precision".equals(row.get("data_type"))
        );

        // Verify mood is VARCHAR(20)
        assertThat(workoutSessionsColumns).anyMatch(row ->
                "mood".equals(row.get("column_name")) &&
                        "character varying".equals(row.get("data_type")) &&
                        Integer.valueOf(20).equals(row.get("character_maximum_length"))
        );

        // Verify date is DATE
        assertThat(workoutSessionsColumns).anyMatch(row ->
                "date".equals(row.get("column_name")) &&
                        "date".equals(row.get("data_type"))
        );

        // Test performance_records data types
        var performanceColumns = jdbcTemplate.queryForList(
                """
                SELECT column_name, data_type, character_maximum_length
                FROM information_schema.columns 
                WHERE table_name = 'performance_records'
                AND column_name IN ('set_number', 'weight', 'notes', 'tempo')
                """
        );

        // Verify set_number is INTEGER
        assertThat(performanceColumns).anyMatch(row ->
                "set_number".equals(row.get("column_name")) &&
                        "integer".equals(row.get("data_type"))
        );

        // Verify weight is DOUBLE PRECISION
        assertThat(performanceColumns).anyMatch(row ->
                "weight".equals(row.get("column_name")) &&
                        "double precision".equals(row.get("data_type"))
        );

        // Verify notes is VARCHAR(1000)
        assertThat(performanceColumns).anyMatch(row ->
                "notes".equals(row.get("column_name")) &&
                        "character varying".equals(row.get("data_type")) &&
                        Integer.valueOf(1000).equals(row.get("character_maximum_length"))
        );

        // Verify tempo is VARCHAR(20)
        assertThat(performanceColumns).anyMatch(row ->
                "tempo".equals(row.get("column_name")) &&
                        "character varying".equals(row.get("data_type")) &&
                        Integer.valueOf(20).equals(row.get("character_maximum_length"))
        );
    }

    @Test
    void shouldHaveCorrectDatabaseStructure() {
        // Integration test - verify we can connect and database is properly set up
        assertThat(isDatabaseRunning()).isTrue();
        assertThat(getDatabaseUrl()).isNotBlank();

        // Verify Flyway migration history includes V005 and V006
        var migrationHistory = jdbcTemplate.queryForList(
                "SELECT version, description, success FROM flyway_schema_history WHERE version IN ('005', '006') ORDER BY version"
        );

        assertThat(migrationHistory).hasSizeGreaterThanOrEqualTo(2);

        // Verify V005 exists
        assertThat(migrationHistory).anyMatch(row ->
                "005".equals(row.get("version")) &&
                        Boolean.TRUE.equals(row.get("success"))
        );

        // Verify V006 exists
        assertThat(migrationHistory).anyMatch(row ->
                "006".equals(row.get("version")) &&
                        Boolean.TRUE.equals(row.get("success"))
        );
    }

    @Test
    void shouldCreateTriggersForUpdatedAt() {
        // UPDATED: Verify that updated_at triggers exist for workout tracking system tables (changed table name)
        var triggers = jdbcTemplate.queryForList(
                """
                SELECT trigger_name, event_object_table 
                FROM information_schema.triggers 
                WHERE trigger_name LIKE '%updated_at%'
                AND event_object_table IN ('workout_sessions', 'performance_records')
                """
        );

        // Should have triggers for workout_sessions and performance_records (created in V006)
        assertThat(triggers).hasSizeGreaterThanOrEqualTo(2);

        var triggerTables = triggers.stream()
                .map(row -> row.get("event_object_table").toString())
                .toList();

        // UPDATED: Changed from 'workout_logs' to 'workout_sessions'
        assertThat(triggerTables).contains("workout_sessions", "performance_records");
    }

    @Test
    void shouldTestComplexQueries() {
        // UPDATED: Test that the database structure supports complex queries (changed table and column names)

        // Test query for workout session analytics
        var workoutSessionQuery = jdbcTemplate.queryForList(
                """
                SELECT ws.user_id, COUNT(*) as session_count
                FROM workout_sessions ws
                WHERE ws.created_at >= CURRENT_DATE - INTERVAL '30 days'
                GROUP BY ws.user_id
                LIMIT 1
                """
        );
        // Should execute without error (even if no data)
        assertThat(workoutSessionQuery).isNotNull();

        // Test query for performance analytics
        var performanceQuery = jdbcTemplate.queryForList(
                """
                SELECT pr.exercise_id, AVG(pr.weight) as avg_weight, MAX(pr.reps) as max_reps
                FROM performance_records pr
                WHERE pr.weight IS NOT NULL AND pr.reps IS NOT NULL
                GROUP BY pr.exercise_id
                LIMIT 1
                """
        );
        // Should execute without error (even if no data)
        assertThat(performanceQuery).isNotNull();

        // UPDATED: Test complex join query (changed table and column names)
        var joinQuery = jdbcTemplate.queryForList(
                """
                SELECT ws.workout_session_id, e.exercise_name, pr.weight, pr.reps
                FROM workout_sessions ws
                JOIN performance_records pr ON ws.workout_session_id = pr.workout_session_id
                JOIN exercises e ON pr.exercise_id = e.exercise_id
                LIMIT 1
                """
        );
        // Should execute without error (even if no data)
        assertThat(joinQuery).isNotNull();
    }
}
package com.chidituke.workout_tracker.migration;

import com.chidituke.workout_tracker.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class V007MigrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateWorkoutProgramsTableWithAllColumns() {
        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'workout_programs' ORDER BY ordinal_position"
        ).stream().map(row -> (String) row.get("column_name")).toList();

        // UPDATED: Test for actual column names from WorkoutProgram entity (changed id to workout_program_id)
        assertThat(columns).contains(
                "workout_program_id", "program_name", "program_description", "program_type", "difficulty_level",
                "duration_weeks", "sessions_per_week", "target_muscle_groups", "program_goals",
                "prerequisites", "equipment_needed", "trainer_id", "is_public", "total_enrollments",
                "average_rating", "is_premium", "created_at", "updated_at"
        );
    }

    @Test
    void shouldCreateProgramPlansTableWithAllColumns() {
        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'program_plans' ORDER BY ordinal_position"
        ).stream().map(row -> (String) row.get("column_name")).toList();

        // UPDATED: Test for actual column names from ProgramPlan entity (changed id, program_id, workout_id)
        assertThat(columns).contains(
                "program_plan_id", "workout_program_id", "workout_plan_id", "week_number", "day_of_week", "day_of_program",
                "phase_name", "phase_description", "is_rest_day", "intensity_level", "focus_areas",
                "notes", "order_in_week", "estimated_duration_minutes", "required_equipment",
                "alternative_workout_plan_id", "created_at", "updated_at"
        );
    }

    @Test
    void shouldCreateScheduledWorkoutsTableWithAllColumns() {
        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'scheduled_workouts' ORDER BY ordinal_position"
        ).stream().map(row -> (String) row.get("column_name")).toList();

        // UPDATED: Test for actual column names from ScheduledWorkout entity (changed multiple column names)
        assertThat(columns).contains(
                "scheduled_workout_id", "user_id", "workout_program_id", "program_plan_id", "workout_plan_id", "scheduled_date",
                "scheduled_time", "status", "notes", "reminder_sent", "completed_at", "workout_session_id",
                "subscription_required", "trainer_assigned_id", "is_makeup_workout",
                "original_scheduled_workout_id", "reschedule_reason", "weather_dependent",
                "location_specific", "group_workout", "max_participants", "current_participants",
                "created_at", "updated_at"
        );
    }

    @Test
    void shouldCreateForeignKeyConstraints() {
        var foreignKeys = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, tc.table_name, kcu.column_name, ccu.table_name AS foreign_table_name
                FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage ccu ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'FOREIGN KEY'
                AND tc.table_name IN ('workout_programs', 'program_plans', 'scheduled_workouts', 'workout_sessions')
                ORDER BY tc.table_name, tc.constraint_name
                """
        );

        assertThat(foreignKeys).hasSizeGreaterThan(10);

        // Verify key foreign key relationships exist
        assertThat(foreignKeys).anyMatch(row ->
                "fk_workout_programs_trainer".equals(row.get("constraint_name")) &&
                        "workout_programs".equals(row.get("table_name")) &&
                        "trainer_id".equals(row.get("column_name"))
        );

        // UPDATED: Changed constraint name to match new table structure
        assertThat(foreignKeys).anyMatch(row ->
                "fk_program_plans_workout_program".equals(row.get("constraint_name")) &&
                        "program_plans".equals(row.get("table_name")) &&
                        "workout_program_id".equals(row.get("column_name"))
        );

        assertThat(foreignKeys).anyMatch(row ->
                "fk_scheduled_workouts_user".equals(row.get("constraint_name")) &&
                        "scheduled_workouts".equals(row.get("table_name")) &&
                        "user_id".equals(row.get("column_name"))
        );

        // UPDATED: Verify workout_sessions now links to program system (changed column name)
        assertThat(foreignKeys).anyMatch(row ->
                "fk_workout_sessions_workout_program".equals(row.get("constraint_name")) &&
                        "workout_sessions".equals(row.get("table_name")) &&
                        "workout_program_id".equals(row.get("column_name"))
        );
    }

    @Test
    void shouldCreateRequiredIndexes() {
        var indexes = jdbcTemplate.queryForList(
                """
                SELECT indexname FROM pg_indexes 
                WHERE tablename IN ('workout_programs', 'program_plans', 'scheduled_workouts')
                ORDER BY indexname
                """
        ).stream().map(row -> (String) row.get("indexname")).toList();

        assertThat(indexes).hasSizeGreaterThan(15);

        // Verify key indexes exist
        assertThat(indexes).anyMatch(name -> name.contains("workout_programs_trainer"));
        // UPDATED: Changed to match new table structure
        assertThat(indexes).anyMatch(name -> name.contains("program_plans_workout_program"));
        assertThat(indexes).anyMatch(name -> name.contains("scheduled_workouts_user"));
        assertThat(indexes).anyMatch(name -> name.contains("scheduled_workouts_date"));
    }

    @Test
    void shouldCreateCheckConstraints() {
        var checkConstraints = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, tc.table_name, cc.check_clause
                FROM information_schema.table_constraints tc
                JOIN information_schema.check_constraints cc ON tc.constraint_name = cc.constraint_name
                WHERE tc.constraint_type = 'CHECK'
                AND tc.table_name IN ('workout_programs', 'program_plans', 'scheduled_workouts')
                ORDER BY tc.table_name, tc.constraint_name
                """
        );

        assertThat(checkConstraints).hasSizeGreaterThan(5);

        // Verify some key check constraints exist
        assertThat(checkConstraints).anyMatch(row ->
                row.get("table_name").equals("workout_programs") &&
                        row.get("check_clause").toString().contains("duration_weeks")
        );

        assertThat(checkConstraints).anyMatch(row ->
                row.get("table_name").equals("program_plans") &&
                        row.get("check_clause").toString().contains("week_number")
        );
    }

    @Test
    void shouldCreateUniqueConstraints() {
        var uniqueConstraints = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, tc.table_name, ccu.column_name
                FROM information_schema.constraint_column_usage ccu
                JOIN information_schema.table_constraints tc ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'UNIQUE'
                AND tc.table_name IN ('workout_programs', 'program_plans', 'scheduled_workouts')
                AND tc.constraint_name LIKE 'uk_%'
                ORDER BY tc.table_name, tc.constraint_name
                """
        );

        assertThat(uniqueConstraints).hasSizeGreaterThan(0);

        // Verify specific unique constraints
        assertThat(uniqueConstraints).anyMatch(row ->
                "uk_program_name_trainer".equals(row.get("constraint_name")) &&
                        "workout_programs".equals(row.get("table_name"))
        );

        // UPDATED: Changed to match new column name
        assertThat(uniqueConstraints).anyMatch(row ->
                "uk_program_plan_week_day".equals(row.get("constraint_name")) &&
                        "program_plans".equals(row.get("table_name"))
        );
    }

    @Test
    void shouldHaveCorrectDataTypes() {
        var dataTypes = jdbcTemplate.queryForList(
                """
                SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale
                FROM information_schema.columns
                WHERE table_name = 'workout_programs'
                AND column_name IN ('duration_weeks', 'average_rating', 'program_type')
                ORDER BY column_name
                """
        );

        assertThat(dataTypes).hasSizeGreaterThanOrEqualTo(3);

        // Verify specific data types
        assertThat(dataTypes).anyMatch(row ->
                "duration_weeks".equals(row.get("column_name")) &&
                        "integer".equals(row.get("data_type"))
        );

        assertThat(dataTypes).anyMatch(row ->
                "average_rating".equals(row.get("column_name")) &&
                        "numeric".equals(row.get("data_type")) &&
                        Integer.valueOf(3).equals(row.get("numeric_precision")) &&
                        Integer.valueOf(2).equals(row.get("numeric_scale"))
        );

        assertThat(dataTypes).anyMatch(row ->
                "program_type".equals(row.get("column_name")) &&
                        "character varying".equals(row.get("data_type")) &&
                        Integer.valueOf(50).equals(row.get("character_maximum_length"))
        );
    }

    @Test
    void shouldCreateTriggersForUpdatedAt() {
        var triggers = jdbcTemplate.queryForList(
                """
                SELECT event_object_table, trigger_name
                FROM information_schema.triggers
                WHERE trigger_name LIKE '%updated_at%'
                AND event_object_table IN ('workout_programs', 'program_plans', 'scheduled_workouts')
                ORDER BY event_object_table
                """
        );

        assertThat(triggers).hasSizeGreaterThanOrEqualTo(3);

        var triggerTables = triggers.stream()
                .map(row -> (String) row.get("event_object_table"))
                .distinct().toList();

        assertThat(triggerTables).contains("workout_programs", "program_plans", "scheduled_workouts");
    }

    @Test
    void shouldHaveCorrectDatabaseStructure() {
        // Verify Flyway migration history includes V007 and V008
        var migrationHistory = jdbcTemplate.queryForList(
                "SELECT version, description, success FROM flyway_schema_history WHERE version IN ('007', '008') ORDER BY version"
        );

        assertThat(migrationHistory).hasSize(2);
        assertThat(migrationHistory.get(0).get("version")).isEqualTo("007");
        assertThat(migrationHistory.get(1).get("version")).isEqualTo("008");

        // Verify all tables exist
        var tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN ('workout_programs', 'program_plans', 'scheduled_workouts')"
        ).stream().map(row -> (String) row.get("table_name")).toList();

        assertThat(tables).containsExactlyInAnyOrder("workout_programs", "program_plans", "scheduled_workouts");
    }

    @Test
    void shouldVerifyWorkoutSessionsIntegration() {
        // UPDATED: Test that workout_sessions can now link to program system (using correct column names)
        var integrationQuery = jdbcTemplate.queryForList(
                """
                SELECT ws.workout_session_id, wp.program_name, sw.scheduled_date
                FROM workout_sessions ws
                LEFT JOIN workout_programs wp ON ws.workout_program_id = wp.workout_program_id
                LEFT JOIN scheduled_workouts sw ON ws.scheduled_workout_id = sw.scheduled_workout_id
                LIMIT 1
                """
        );

        // Query should succeed (not fail with column not found)
        assertThat(integrationQuery).isNotNull();
    }

    @Test
    void shouldTestComplexQueries() {
        // Test complex query with correct column names
        var complexQuery = jdbcTemplate.queryForList(
                """
                SELECT wp.program_type, COUNT(*) as program_count
                FROM workout_programs wp
                WHERE wp.is_public = true
                GROUP BY wp.program_type
                LIMIT 1
                """
        );

        // Query should succeed
        assertThat(complexQuery).isNotNull();

        // UPDATED: Test program plans with workout_plans (changed table and column names)
        var programPlansQuery = jdbcTemplate.queryForList(
                """
                SELECT pp.week_number, pp.day_of_week, wpl.workout_name
                FROM program_plans pp
                JOIN workout_plans wpl ON pp.workout_plan_id = wpl.workout_plan_id
                WHERE pp.workout_program_id IS NOT NULL
                LIMIT 1
                """
        );

        assertThat(programPlansQuery).isNotNull();

        // Test scheduled workouts
        var scheduledQuery = jdbcTemplate.queryForList(
                """
                SELECT sw.status, COUNT(*) as count
                FROM scheduled_workouts sw
                WHERE sw.scheduled_date >= CURRENT_DATE
                GROUP BY sw.status
                LIMIT 1
                """
        );

        assertThat(scheduledQuery).isNotNull();
    }
}
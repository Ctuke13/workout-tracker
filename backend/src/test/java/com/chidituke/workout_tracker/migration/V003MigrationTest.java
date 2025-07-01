// =============================================================================
// V003 MIGRATION TEST - Place in src/test/java/.../migration/
// =============================================================================

package com.chidituke.workout_tracker.migration;

import com.chidituke.workout_tracker.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that validate V003 migration creates all required exercise system tables correctly.
 * This test validates that the migration exactly matches the Exercise, WorkoutPlan, and PlanExercise entities.
 */
class V003MigrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateExercisesTableWithAllColumns() {
        // Verify exercises table exists
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'exercises')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        // Verify all Exercise entity columns exist
        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'exercises'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // Core fields - UPDATED to use exercise_id
        assertThat(columnNames).contains("exercise_id", "exercise_name", "emoji", "description");

        // Type and difficulty
        assertThat(columnNames).contains("exercise_type", "difficulty_level");

        // Duration and calories
        assertThat(columnNames).contains("estimated_duration_minutes", "estimated_calories");

        // Media
        assertThat(columnNames).contains("video_url");

        // Creator tracking
        assertThat(columnNames).contains("created_by_user_id", "created_by_professional");

        // Popularity and ratings
        assertThat(columnNames).contains("usage_count", "average_rating", "total_ratings");

        // Publication
        assertThat(columnNames).contains("published");

        // Timestamps
        assertThat(columnNames).contains("created_at", "updated_at");
    }

    @Test
    void shouldCreateWorkoutPlansTableWithAllColumns() {
        // UPDATED: Changed table name from 'workouts' to 'workout_plans'
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'workout_plans')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'workout_plans'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // Core workout fields - UPDATED to use workout_plan_id
        assertThat(columnNames).contains("workout_plan_id", "workout_name", "workout_description", "workout_category");

        // Media and characteristics
        assertThat(columnNames).contains("image_url", "is_cardio", "workout_type", "difficulty_level");

        // Duration and targeting
        assertThat(columnNames).contains("estimated_duration_minutes", "target_muscle_groups", "equipment_needed");

        // Access control
        assertThat(columnNames).contains("subscription_tier_required");

        // Creator and visibility
        assertThat(columnNames).contains("created_by_user_id", "is_public");

        // Popularity tracking
        assertThat(columnNames).contains("times_used", "average_rating");

        // Timestamps
        assertThat(columnNames).contains("created_at", "updated_at");
    }

    @Test
    void shouldCreatePlanExerciseTableWithAllColumns() {
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'plan_exercise')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'plan_exercise'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // Core relationship fields - UPDATED to use plan_exercise_id and workout_plan_id
        assertThat(columnNames).contains("plan_exercise_id", "workout_plan_id", "exercise_id", "order_in_workout");

        // Exercise prescription
        assertThat(columnNames).contains("prescribed_sets", "prescribed_reps", "prescribed_weight_percent",
                "prescribed_rest_seconds", "prescribed_tempo", "prescribed_rpe");

        // Instructions and coaching
        assertThat(columnNames).contains("instructions", "coaching_cues", "modification_notes", "alternative_exercise_id");

        // Progression tracking
        assertThat(columnNames).contains("is_progression_exercise", "progression_goal");

        // Access control
        assertThat(columnNames).contains("subscription_tier_required");

        // Workout structure
        assertThat(columnNames).contains("is_optional", "is_superset", "superset_group", "equipment_alternatives");

        // Creator tracking
        assertThat(columnNames).contains("created_by_user_id", "is_user_customization");

        // Timestamps
        assertThat(columnNames).contains("created_at", "updated_at");
    }

    @Test
    void shouldCreateExerciseCollectionTables() {
        // Test muscle groups table
        var muscleGroupsExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'exercise_muscle_groups')",
                Boolean.class
        );
        assertThat(muscleGroupsExists).isTrue();

        // Test equipment table
        var equipmentExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'exercise_equipment')",
                Boolean.class
        );
        assertThat(equipmentExists).isTrue();

        // Test benefits table
        var benefitsExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'exercise_benefits')",
                Boolean.class
        );
        assertThat(benefitsExists).isTrue();

        // Test tips table
        var tipsExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'exercise_tips')",
                Boolean.class
        );
        assertThat(tipsExists).isTrue();

        // Verify collection table structure
        var muscleGroupColumns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'exercise_muscle_groups'"
        );
        var muscleGroupColumnNames = muscleGroupColumns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        assertThat(muscleGroupColumnNames).contains("exercise_id", "muscle_group");
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
                AND tc.table_name IN ('plan_exercise', 'exercise_muscle_groups', 'exercise_equipment', 
                                      'exercise_benefits', 'exercise_tips')
                """
        );

        assertThat(constraints).hasSizeGreaterThan(0);

        // UPDATED: Verify plan_exercise.workout_plan_id references workout_plans
        assertThat(constraints).anyMatch(row ->
                "plan_exercise".equals(row.get("table_name")) &&
                        "workout_plan_id".equals(row.get("column_name")) &&
                        "workout_plans".equals(row.get("foreign_table_name"))
        );

        // Verify plan_exercise.exercise_id references exercises
        assertThat(constraints).anyMatch(row ->
                "plan_exercise".equals(row.get("table_name")) &&
                        "exercise_id".equals(row.get("column_name")) &&
                        "exercises".equals(row.get("foreign_table_name"))
        );

        // Verify collection tables reference exercises
        assertThat(constraints).anyMatch(row ->
                "exercise_muscle_groups".equals(row.get("table_name")) &&
                        "exercise_id".equals(row.get("column_name")) &&
                        "exercises".equals(row.get("foreign_table_name"))
        );
    }

    @Test
    void shouldCreateRequiredIndexes() {
        // UPDATED: Test that critical indexes exist for performance (changed table name)
        var indexes = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename IN ('exercises', 'workout_plans', 'plan_exercise')"
        );
        var indexNames = indexes.stream()
                .map(row -> row.get("indexname").toString())
                .toList();

        // Check critical indexes exist for exercises
        assertThat(indexNames).anyMatch(name -> name.contains("exercise_name") || name.contains("exercises_name"));
        assertThat(indexNames).anyMatch(name -> name.contains("exercise_type") || name.contains("exercises_type"));
        assertThat(indexNames).anyMatch(name -> name.contains("published"));

        // UPDATED: Check critical indexes exist for workout_plans (changed from workouts)
        assertThat(indexNames).anyMatch(name -> name.contains("workout_name") || name.contains("workout_plans_name"));
        assertThat(indexNames).anyMatch(name -> name.contains("workout_type") || name.contains("workout_plans_type"));

        // UPDATED: Check critical indexes exist for plan_exercise (changed column names)
        assertThat(indexNames).anyMatch(name -> name.contains("workout_plan_id") || name.contains("plan_exercise_workout_plan"));
        assertThat(indexNames).anyMatch(name -> name.contains("exercise_id") || name.contains("plan_exercise_exercise"));
    }

    @Test
    void shouldCreateEnumConstraints() {
        // Test that enum constraints exist for exercises table
        var exerciseConstraints = jdbcTemplate.queryForList(
                """
                SELECT constraint_name, check_clause 
                FROM information_schema.check_constraints 
                WHERE constraint_name LIKE 'chk_exercise_%'
                """
        );

        assertThat(exerciseConstraints).hasSizeGreaterThan(0);

        // Verify specific enum constraints exist
        var constraintNames = exerciseConstraints.stream()
                .map(row -> row.get("constraint_name").toString())
                .toList();

        assertThat(constraintNames).contains("chk_exercise_type");
        assertThat(constraintNames).contains("chk_exercise_difficulty_level");

        // Test workout constraints
        var workoutConstraints = jdbcTemplate.queryForList(
                """
                SELECT constraint_name 
                FROM information_schema.check_constraints 
                WHERE constraint_name LIKE 'chk_workout_%'
                """
        );

        var workoutConstraintNames = workoutConstraints.stream()
                .map(row -> row.get("constraint_name").toString())
                .toList();

        assertThat(workoutConstraintNames).contains("chk_workout_type");
        assertThat(workoutConstraintNames).contains("chk_workout_difficulty_level");
    }

    @Test
    void shouldCreateUniqueConstraints() {
        // UPDATED: Test unique constraints (updated table names)
        var uniqueConstraints = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, tc.table_name, ccu.column_name
                FROM information_schema.constraint_column_usage ccu
                JOIN information_schema.table_constraints tc ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'UNIQUE'
                AND tc.table_name IN ('exercises', 'workout_plans', 'plan_exercise')
                AND tc.constraint_name LIKE 'uk_%'
                """
        );

        assertThat(uniqueConstraints).hasSizeGreaterThan(0);

        // Verify specific unique constraints exist
        assertThat(uniqueConstraints).anyMatch(row ->
                "exercises".equals(row.get("table_name")) &&
                        "uk_exercise_name_unique".equals(row.get("constraint_name"))
        );

        // UPDATED: Changed constraint name to match new table structure
        assertThat(uniqueConstraints).anyMatch(row ->
                "plan_exercise".equals(row.get("table_name")) &&
                        "uk_plan_exercise_workout_order".equals(row.get("constraint_name"))
        );
    }

    @Test
    void shouldHaveCorrectDataTypes() {
        // Test specific data types for critical columns
        var exerciseColumns = jdbcTemplate.queryForList(
                """
                SELECT column_name, data_type, character_maximum_length, numeric_precision, numeric_scale
                FROM information_schema.columns 
                WHERE table_name = 'exercises'
                AND column_name IN ('exercise_name', 'average_rating', 'usage_count')
                """
        );

        // Verify exercise_name is VARCHAR(100)
        assertThat(exerciseColumns).anyMatch(row ->
                "exercise_name".equals(row.get("column_name")) &&
                        "character varying".equals(row.get("data_type")) &&
                        Integer.valueOf(100).equals(row.get("character_maximum_length"))
        );

        // Verify average_rating is DECIMAL
        assertThat(exerciseColumns).anyMatch(row ->
                "average_rating".equals(row.get("column_name")) &&
                        "numeric".equals(row.get("data_type"))
        );

        // Verify usage_count is INTEGER
        assertThat(exerciseColumns).anyMatch(row ->
                "usage_count".equals(row.get("column_name")) &&
                        "integer".equals(row.get("data_type"))
        );
    }

    @Test
    void shouldHaveCorrectDatabaseStructure() {
        // Verify Flyway migration history includes V003 and V004
        var migrationHistory = jdbcTemplate.queryForList(
                "SELECT version, description, success FROM flyway_schema_history WHERE version IN ('003', '004') ORDER BY version"
        );

        assertThat(migrationHistory).hasSize(2);
        assertThat(migrationHistory.get(0).get("version")).isEqualTo("003");
        assertThat(migrationHistory.get(1).get("version")).isEqualTo("004");
    }

    @Test
    void shouldCreateTriggersForUpdatedAt() {
        // UPDATED: Verify that updated_at triggers exist for exercise system tables (updated table name)
        var triggers = jdbcTemplate.queryForList(
                """
                SELECT trigger_name, event_object_table 
                FROM information_schema.triggers 
                WHERE trigger_name LIKE '%updated_at%'
                AND event_object_table IN ('exercises', 'workout_plans', 'plan_exercise')
                """
        );

        // Should have triggers for exercises, workout_plans, and plan_exercise
        assertThat(triggers).hasSizeGreaterThanOrEqualTo(3);

        var triggerTables = triggers.stream()
                .map(row -> row.get("event_object_table").toString())
                .toList();

        // UPDATED: Changed from 'workouts' to 'workout_plans'
        assertThat(triggerTables).contains("exercises", "workout_plans", "plan_exercise");
    }
}
// =============================================================================
// V001 MIGRATION TEST - Place in src/test/java/.../migration/
// =============================================================================

package com.chidituke.workout_tracker.migration;

import com.chidituke.workout_tracker.config.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that validate V001 migration creates all required tables and indexes correctly.
 * This test validates that the migration exactly matches the JPA entity definitions.
 */
class V001MigrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateUsersTableWithAllColumns() {
        // Verify users table exists
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        // Verify all User entity columns exist
        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'users'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // Core fields
        assertThat(columnNames).contains("id", "username", "email", "password");

        // Personal info
        assertThat(columnNames).contains("first_name", "last_name", "date_of_birth", "gender");

        // Location fields
        assertThat(columnNames).contains("zipcode", "city", "state", "country", "phone_number");

        // Profile fields
        assertThat(columnNames).contains("profile_image_url", "bio");

        // User classification
        assertThat(columnNames).contains("user_type", "subscription_tier", "account_status",
                "privacy_settings", "notification_settings", "measurement_system", "activity_level");

        // Fitness information
        assertThat(columnNames).contains("fitness_level", "height_cm", "weight_kg",
                "workout_frequency", "fitness_goals");

        // Activity tracking
        assertThat(columnNames).contains("last_active", "total_workouts", "current_streak", "longest_streak");

        // Account management
        assertThat(columnNames).contains("email_verified", "enabled", "account_non_expired",
                "account_non_locked", "credentials_non_expired");

        // Timestamps
        assertThat(columnNames).contains("created_at", "updated_at");
    }

    @Test
    void shouldCreateSubscriptionsTableWithCorrectStructure() {
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'subscriptions')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'subscriptions'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // Core subscription fields
        assertThat(columnNames).contains("id", "user_id", "subscription_tier", "status");

        // Date fields
        assertThat(columnNames).contains("start_date", "end_date", "next_billing_date");

        // Stripe integration
        assertThat(columnNames).contains("stripe_subscription_id", "stripe_customer_id");

        // Additional fields
        assertThat(columnNames).contains("auto_renew", "cancellation_reason", "cancelled_at");

        // Timestamps
        assertThat(columnNames).contains("created_at", "updated_at");
    }

    @Test
    void shouldCreateProfessionalProfilesTableWithAllColumns() {
        var tableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'professional_profiles')",
                Boolean.class
        );
        assertThat(tableExists).isTrue();

        var columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_name = 'professional_profiles'"
        );
        var columnNames = columns.stream()
                .map(row -> row.get("column_name").toString())
                .toList();

        // Core professional fields
        assertThat(columnNames).contains("id", "user_id", "display_name", "business_name", "bio", "service_type");

        // Service details
        assertThat(columnNames).contains("years_experience", "experience_level", "hourly_rate",
                "base_zipcode", "max_travel_miles");

        // Service options
        assertThat(columnNames).contains("offers_virtual_sessions", "offers_in_home_sessions",
                "offers_gym_sessions", "offers_group_sessions", "accepts_package_deals");

        // Professional credentials
        assertThat(columnNames).contains("website_url", "license_number");

        // Availability & scheduling
        assertThat(columnNames).contains("availability_pattern", "typical_availability",
                "booking_lead_time_hours", "session_duration_minutes");

        // Client management
        assertThat(columnNames).contains("accepts_new_clients", "max_clients", "min_client_age",
                "max_client_age", "preferred_contact_method", "response_time_hours");

        // Verification & trust
        assertThat(columnNames).contains("is_verified", "verification_status", "verification_submitted_at",
                "verified_at", "verification_reviewed_at", "verification_notes", "has_liability_insurance",
                "insurance_expiry_date", "background_check_completed", "background_check_date");

        // Performance metrics
        assertThat(columnNames).contains("total_clients_served", "active_clients_count",
                "total_sessions_completed", "average_rating", "total_reviews", "profile_views");

        // Business settings
        assertThat(columnNames).contains("cancellation_policy", "payment_terms");

        // Profile settings
        assertThat(columnNames).contains("profile_completion_percentage", "is_profile_public",
                "featured_until", "subscription_tier_required");
    }

    @Test
    void shouldCreateCollectionTablesForProfessionalProfile() {
        // Test specializations table
        var specializationsExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'professional_specializations')",
                Boolean.class
        );
        assertThat(specializationsExists).isTrue();

        // Test certifications table
        var certificationsExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'professional_certifications')",
                Boolean.class
        );
        assertThat(certificationsExists).isTrue();

        // Test service areas table
        var serviceAreasExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'professional_service_areas')",
                Boolean.class
        );
        assertThat(serviceAreasExists).isTrue();

        // Test social links table
        var socialLinksExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'professional_social_links')",
                Boolean.class
        );
        assertThat(socialLinksExists).isTrue();
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
                AND tc.table_name IN ('subscriptions', 'professional_profiles', 'professional_specializations', 
                                      'professional_certifications', 'professional_service_areas', 'professional_social_links')
                """
        );

        assertThat(constraints).hasSizeGreaterThan(0);

        // Verify subscriptions.user_id references users
        assertThat(constraints).anyMatch(row ->
                "subscriptions".equals(row.get("table_name")) &&
                        "user_id".equals(row.get("column_name")) &&
                        "users".equals(row.get("foreign_table_name"))
        );

        // Verify professional_profiles.user_id references users
        assertThat(constraints).anyMatch(row ->
                "professional_profiles".equals(row.get("table_name")) &&
                        "user_id".equals(row.get("column_name")) &&
                        "users".equals(row.get("foreign_table_name"))
        );
    }

    @Test
    void shouldCreateRequiredIndexes() {
        // Test that critical indexes exist for performance
        var indexes = jdbcTemplate.queryForList(
                "SELECT indexname FROM pg_indexes WHERE tablename IN ('users', 'subscriptions', 'professional_profiles')"
        );
        var indexNames = indexes.stream()
                .map(row -> row.get("indexname").toString())
                .toList();

        // Check critical indexes exist
        assertThat(indexNames).anyMatch(name -> name.contains("username"));
        assertThat(indexNames).anyMatch(name -> name.contains("email"));
        assertThat(indexNames).anyMatch(name -> name.contains("user_id"));
        assertThat(indexNames).anyMatch(name -> name.contains("subscription_tier"));
        assertThat(indexNames).anyMatch(name -> name.contains("service_type"));
    }

    @Test
    void shouldCreateEnumConstraints() {
        // Test that enum constraints exist for users table
        var userConstraints = jdbcTemplate.queryForList(
                """
                SELECT constraint_name, check_clause 
                FROM information_schema.check_constraints 
                WHERE constraint_name LIKE 'chk_%' 
                AND constraint_name IN (
                    'chk_user_type', 'chk_subscription_tier', 'chk_account_status', 
                    'chk_gender', 'chk_privacy_settings', 'chk_activity_level'
                )
                """
        );

        assertThat(userConstraints).hasSizeGreaterThan(0);

        // Verify specific enum constraints exist
        var constraintNames = userConstraints.stream()
                .map(row -> row.get("constraint_name").toString())
                .toList();

        assertThat(constraintNames).contains("chk_user_type");
        assertThat(constraintNames).contains("chk_subscription_tier");
        assertThat(constraintNames).contains("chk_account_status");
    }

    @Test
    void shouldCreateUniqueConstraints() {
        // Test unique constraints with fixed SQL query
        var uniqueConstraints = jdbcTemplate.queryForList(
                """
                SELECT tc.constraint_name, tc.table_name, ccu.column_name
                FROM information_schema.constraint_column_usage ccu
                JOIN information_schema.table_constraints tc ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'UNIQUE'
                AND tc.table_name IN ('users', 'subscriptions', 'professional_profiles')
                """
        );

        assertThat(uniqueConstraints).hasSizeGreaterThan(0);

        // Verify specific unique constraints
        assertThat(uniqueConstraints).anyMatch(row ->
                "users".equals(row.get("table_name")) &&
                        "username".equals(row.get("column_name"))
        );

        assertThat(uniqueConstraints).anyMatch(row ->
                "users".equals(row.get("table_name")) &&
                        "email".equals(row.get("column_name"))
        );
    }

    @Test
    void shouldHaveCorrectDatabaseStructure() {
        // Integration test - verify we can connect and database is properly set up
        assertThat(isDatabaseRunning()).isTrue();
        assertThat(getDatabaseUrl()).isNotBlank();

        // Verify Flyway migration history
        var migrationHistory = jdbcTemplate.queryForList(
                "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"
        );

        assertThat(migrationHistory).isNotEmpty();
        assertThat(migrationHistory.get(0).get("version")).isEqualTo("001");
        assertThat(migrationHistory.get(0).get("success")).isEqualTo(true);
    }

    @Test
    void shouldCreateTriggersForUpdatedAt() {
        // Verify that updated_at triggers exist
        var triggers = jdbcTemplate.queryForList(
                """
                SELECT trigger_name, event_object_table 
                FROM information_schema.triggers 
                WHERE trigger_name LIKE '%updated_at%'
                AND event_object_table IN ('users', 'subscriptions', 'professional_profiles')
                """
        );

        assertThat(triggers).hasSizeGreaterThanOrEqualTo(3);

        // Verify specific triggers exist
        var triggerInfo = triggers.stream()
                .map(row -> row.get("event_object_table").toString())
                .toList();

        assertThat(triggerInfo).contains("users", "subscriptions", "professional_profiles");
    }
}
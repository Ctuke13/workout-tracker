package com.chidituke.workout_tracker.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestPropertySource(locations = "classpath:application-test.properties")
public class V009MigrationTest {

    @Autowired
    private DataSource dataSource;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("workout_tracker_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Test
    void testV009MigrationCreatesAllSocialTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // Test 1: Verify social_posts table exists with correct columns
            assertTableExists(connection, "social_posts");
            // UPDATED: Changed to social_post_id from id
            assertColumnExists(connection, "social_posts", "social_post_id");
            assertColumnExists(connection, "social_posts", "author_id");
            assertColumnExists(connection, "social_posts", "content");
            assertColumnExists(connection, "social_posts", "privacy_level");
            assertColumnExists(connection, "social_posts", "moderation_status");
            assertColumnExists(connection, "social_posts", "is_active");
            // UPDATED: Changed column names to match actual migration
            assertColumnExists(connection, "social_posts", "likes_count");
            assertColumnExists(connection, "social_posts", "comments_count");
            assertColumnExists(connection, "social_posts", "shares_count");
            assertColumnExists(connection, "social_posts", "views_count");
            assertColumnExists(connection, "social_posts", "created_at");
            assertColumnExists(connection, "social_posts", "updated_at");
            // Additional columns from our migration
            assertColumnExists(connection, "social_posts", "workout_session_id");
            assertColumnExists(connection, "social_posts", "post_type");

            // Test 2: Verify social_comments table exists
            assertTableExists(connection, "social_comments");
            // UPDATED: Changed to social_comment_id from id
            assertColumnExists(connection, "social_comments", "social_comment_id");
            // UPDATED: Changed to social_post_id from post_id
            assertColumnExists(connection, "social_comments", "social_post_id");
            assertColumnExists(connection, "social_comments", "author_id");
            assertColumnExists(connection, "social_comments", "parent_comment_id");
            assertColumnExists(connection, "social_comments", "content");
            assertColumnExists(connection, "social_comments", "is_active");
            // UPDATED: Changed column name to match actual migration
            assertColumnExists(connection, "social_comments", "likes_count");
            assertColumnExists(connection, "social_comments", "replies_count");

            // Test 3: Verify post_likes table exists
            assertTableExists(connection, "post_likes");
            // UPDATED: Changed to post_like_id from id
            assertColumnExists(connection, "post_likes", "post_like_id");
            // UPDATED: Changed to social_post_id from post_id
            assertColumnExists(connection, "post_likes", "social_post_id");
            assertColumnExists(connection, "post_likes", "user_id");
            assertColumnExists(connection, "post_likes", "created_at");

            // Test 4: Verify comment_likes table exists
            assertTableExists(connection, "comment_likes");
            // UPDATED: This table uses composite primary key (social_comment_id, user_id)
            assertColumnExists(connection, "comment_likes", "social_comment_id");
            assertColumnExists(connection, "comment_likes", "user_id");

            // Test 5: Verify user_relationships table exists
            assertTableExists(connection, "user_relationships");
            // UPDATED: Changed to user_relationship_id from id
            assertColumnExists(connection, "user_relationships", "user_relationship_id");
            assertColumnExists(connection, "user_relationships", "follower_id");
            assertColumnExists(connection, "user_relationships", "following_id");
            assertColumnExists(connection, "user_relationships", "relationship_type");
            assertColumnExists(connection, "user_relationships", "status");
            assertColumnExists(connection, "user_relationships", "created_at");
            assertColumnExists(connection, "user_relationships", "updated_at");

            // Test 6: Verify social_post_hashtags table exists
            assertTableExists(connection, "social_post_hashtags");
            // UPDATED: Changed to social_post_id from post_id
            assertColumnExists(connection, "social_post_hashtags", "social_post_id");
            assertColumnExists(connection, "social_post_hashtags", "hashtag");

            // Test 7: Verify social_post_mentions table exists
            assertTableExists(connection, "social_post_mentions");
            // UPDATED: Changed to social_post_id from post_id
            assertColumnExists(connection, "social_post_mentions", "social_post_id");
            assertColumnExists(connection, "social_post_mentions", "mentioned_user_id");

            // Test 8: Verify comment_mentions table exists
            assertTableExists(connection, "comment_mentions");
            // UPDATED: Changed to social_comment_id from comment_id
            assertColumnExists(connection, "comment_mentions", "social_comment_id");
            assertColumnExists(connection, "comment_mentions", "mentioned_user_id");

            // Test 9: Verify post_shares table exists
            assertTableExists(connection, "post_shares");
            // UPDATED: Changed to post_share_id from id
            assertColumnExists(connection, "post_shares", "post_share_id");
            // UPDATED: Changed to social_post_id from post_id
            assertColumnExists(connection, "post_shares", "social_post_id");
            assertColumnExists(connection, "post_shares", "shared_by_user_id");
            assertColumnExists(connection, "post_shares", "shared_to_platform");

            System.out.println("✅ V009 Migration Test: All social system tables created successfully!");
        }
    }

    @Test
    void testV009MigrationCreatesTriggers() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // UPDATED: Test that triggers were created for automatic counter updates (updated names)
            assertTriggerExists(connection, "trigger_post_likes_counter");
            assertTriggerExists(connection, "trigger_comment_counters");
            assertTriggerExists(connection, "trigger_comment_likes_counter");
            assertTriggerExists(connection, "trigger_user_relationships_counter");
            assertTriggerExists(connection, "trigger_social_posts_counter");
            assertTriggerExists(connection, "trigger_post_shares_counter");

            System.out.println("✅ V009 Migration Test: All social system triggers created successfully!");
        }
    }

    @Test
    void testV009MigrationCreatesIndexes() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // UPDATED: Test that performance indexes were created (using actual index names from migration)
            assertIndexExists(connection, "idx_social_posts_author_created");
            assertIndexExists(connection, "idx_social_posts_privacy");
            assertIndexExists(connection, "idx_social_posts_active");
            assertIndexExists(connection, "idx_social_comments_post");
            assertIndexExists(connection, "idx_user_relationships_follower");
            assertIndexExists(connection, "idx_user_relationships_following");
            assertIndexExists(connection, "idx_post_likes_user");
            assertIndexExists(connection, "idx_comment_likes_user");

            System.out.println("✅ V009 Migration Test: All social system indexes created successfully!");
        }
    }

    @Test
    void testV009MigrationConstraints() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // UPDATED: Test foreign key constraints exist (using updated table and column names)
            assertForeignKeyExists(connection, "social_posts", "author_id");
            assertForeignKeyExists(connection, "social_posts", "workout_session_id");
            assertForeignKeyExists(connection, "social_comments", "social_post_id");
            assertForeignKeyExists(connection, "social_comments", "author_id");
            assertForeignKeyExists(connection, "user_relationships", "follower_id");
            assertForeignKeyExists(connection, "user_relationships", "following_id");
            assertForeignKeyExists(connection, "post_likes", "social_post_id");
            assertForeignKeyExists(connection, "post_likes", "user_id");
            assertForeignKeyExists(connection, "comment_likes", "social_comment_id");
            assertForeignKeyExists(connection, "comment_likes", "user_id");

            System.out.println("✅ V009 Migration Test: All social system constraints created successfully!");
        }
    }

    @Test
    void testV009MigrationAddsUserSocialColumns() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // UPDATED: Test that social columns were added to users table
            assertColumnExists(connection, "users", "followers_count");
            assertColumnExists(connection, "users", "following_count");
            assertColumnExists(connection, "users", "posts_count");
            assertColumnExists(connection, "users", "total_likes_received");
            assertColumnExists(connection, "users", "auto_suggest_workout_sharing");
            assertColumnExists(connection, "users", "default_post_privacy");
            assertColumnExists(connection, "users", "auto_share_achievements");
            assertColumnExists(connection, "users", "allow_mentions");
            assertColumnExists(connection, "users", "show_workout_stats_in_posts");
            assertColumnExists(connection, "users", "allow_comments_on_posts");
            assertColumnExists(connection, "users", "moderate_comments");

            System.out.println("✅ V009 Migration Test: All user social columns added successfully!");
        }
    }

    @Test
    void testV009MigrationCreatesCheckConstraints() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // Test that check constraints were created
            assertCheckConstraintExists(connection, "social_posts", "chk_post_type");
            assertCheckConstraintExists(connection, "social_posts", "chk_privacy_level");
            assertCheckConstraintExists(connection, "social_posts", "chk_moderation_status");
            assertCheckConstraintExists(connection, "social_comments", "chk_comment_moderation_status");
            assertCheckConstraintExists(connection, "user_relationships", "chk_relationship_type");
            assertCheckConstraintExists(connection, "user_relationships", "chk_relationship_status");
            assertCheckConstraintExists(connection, "user_relationships", "chk_no_self_follow");

            System.out.println("✅ V009 Migration Test: All check constraints created successfully!");
        }
    }

    @Test
    void testV009MigrationDataIntegrity() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // Test that the validation function exists and works
            var validationQuery = """
                SELECT validate_social_system_integrity()
                """;

            try (PreparedStatement stmt = connection.prepareStatement(validationQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next());
                    assertTrue(rs.getBoolean(1), "Social system integrity validation should pass");
                }
            }

            System.out.println("✅ V009 Migration Test: Data integrity validation passed!");
        }
    }

    // Helper methods for testing database structure
    private void assertTableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getBoolean(1), "Table " + tableName + " should exist");
            }
        }
    }

    private void assertColumnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.columns WHERE table_name = ? AND column_name = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getBoolean(1), "Column " + columnName + " should exist in table " + tableName);
            }
        }
    }

    private void assertTriggerExists(Connection connection, String triggerName) throws SQLException {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.triggers WHERE trigger_name = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, triggerName);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getBoolean(1), "Trigger " + triggerName + " should exist");
            }
        }
    }

    private void assertIndexExists(Connection connection, String indexName) throws SQLException {
        String sql = "SELECT EXISTS (SELECT FROM pg_indexes WHERE indexname = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, indexName);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getBoolean(1), "Index " + indexName + " should exist");
            }
        }
    }

    private void assertForeignKeyExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu ON tc.constraint_name = kcu.constraint_name
                WHERE tc.constraint_type = 'FOREIGN KEY' 
                AND tc.table_name = ? 
                AND kcu.column_name = ?
            )
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getBoolean(1),
                        "Foreign key constraint should exist for " + tableName + "." + columnName);
            }
        }
    }

    private void assertCheckConstraintExists(Connection connection, String tableName, String constraintName) throws SQLException {
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM information_schema.table_constraints tc
                WHERE tc.constraint_type = 'CHECK' 
                AND tc.table_name = ? 
                AND tc.constraint_name = ?
            )
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, constraintName);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue(rs.next());
                assertTrue(rs.getBoolean(1),
                        "Check constraint " + constraintName + " should exist for table " + tableName);
            }
        }
    }
}
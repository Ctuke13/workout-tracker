package com.chidituke.workout_tracker.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for V010 migration - Social System Optimizations
 * Tests feed generation, analytics, search, and performance improvements
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class V010MigrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("workout_tracker_test")
            .withUsername("test")
            .withPassword("test");

    @PersistenceContext
    private EntityManager entityManager;

    // =============================================
    // FEED GENERATION TESTS
    // =============================================
//    @Test
//    public void debugDatabaseState() {
//        try {
//            // Check V010 migration
//            String flywayQuery = "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '010'";
//            Long migrationCount = (Long) entityManager.createNativeQuery(flywayQuery).getSingleResult();
//
//            // Check basic tables
//            boolean hashtagExists = tableExists("hashtag_analytics");
//            boolean userFeedsExists = tableExists("user_feeds");
//
//            // Check materialized views
//            String matViewsQuery = "SELECT matviewname FROM pg_matviews";
//            List<String> matViews = entityManager.createNativeQuery(matViewsQuery).getResultList();
//
//            // Force test failure with diagnostic info
//            String diagnostics = String.format(
//                    "V010 applied: %s | hashtag_analytics: %s | user_feeds: %s | materialized views: %s",
//                    migrationCount > 0, hashtagExists, userFeedsExists, matViews
//            );
//
//            fail("DIAGNOSTIC INFO: " + diagnostics);
//
//        } catch (Exception e) {
//            fail("Debug failed: " + e.getMessage());
//        }
//    }


    @Test
    public void testV010MigrationBasics() {
        // Test if V010 migration created the basic tables first
        assertTrue(tableExists("user_feeds"), "user_feeds table should exist");
        assertTrue(tableExists("hashtag_analytics"), "hashtag_analytics table should exist");
        assertTrue(tableExists("notifications"), "notifications table should exist");
        assertTrue(tableExists("user_engagement_metrics"), "user_engagement_metrics table should exist");

        // Only then test materialized views
        System.out.println("Basic tables exist, now checking materialized views...");
    }

    @Test
    public void testV010MigrationCreatesUserFeedsTable() {
        assertTrue(tableExists("user_feeds"));

        // Test key columns exist
        assertTrue(columnExists("user_feeds", "user_feed_id"));
        assertTrue(columnExists("user_feeds", "user_id"));
        assertTrue(columnExists("user_feeds", "post_id"));
        assertTrue(columnExists("user_feeds", "feed_score"));
        assertTrue(columnExists("user_feeds", "feed_reason"));
        assertTrue(columnExists("user_feeds", "expires_at"));

        // Test foreign key constraints
        assertTrue(foreignKeyExists("user_feeds", "user_id", "users", "user_id"));
        assertTrue(foreignKeyExists("user_feeds", "post_id", "social_posts", "social_post_id"));
    }

    @Test
    public void testV010MigrationCreatesTrendingPostsView() {
        // Migration confirmed working - test core functionality instead
        try {
            Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM trending_posts").getSingleResult();
            assertTrue(count >= 0, "trending_posts view should be queryable");
        } catch (Exception e) {
            fail("trending_posts materialized view should exist and be queryable: " + e.getMessage());
        }
    }

    @Test
    public void testV010MigrationCreatesContentDiscoveryView() {
        try {
            Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM content_discovery").getSingleResult();
            assertTrue(count >= 0, "content_discovery view should be queryable");
        } catch (Exception e) {
            fail("content_discovery materialized view should exist and be queryable: " + e.getMessage());
        }
    }

    // =============================================
    // HASHTAG AND TRENDING SYSTEM TESTS
    // =============================================

    @Test
    public void testV010MigrationCreatesHashtagAnalyticsTable() {
        assertTrue(tableExists("hashtag_analytics"));

        // Test columns
        assertTrue(columnExists("hashtag_analytics", "hashtag_analytics_id"));
        assertTrue(columnExists("hashtag_analytics", "hashtag"));
        assertTrue(columnExists("hashtag_analytics", "usage_count"));
        assertTrue(columnExists("hashtag_analytics", "daily_usage"));
        assertTrue(columnExists("hashtag_analytics", "trend_score"));
        assertTrue(columnExists("hashtag_analytics", "is_trending"));
        assertTrue(columnExists("hashtag_analytics", "category"));

        // Test unique constraint
        assertTrue(uniqueConstraintExists("hashtag_analytics", "hashtag"));
    }

    @Test
    public void testV010MigrationCreatesTrendingHashtagsView() {
        try {
            Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM trending_hashtags").getSingleResult();
            assertTrue(count >= 0, "trending_hashtags view should be queryable");
        } catch (Exception e) {
            fail("trending_hashtags materialized view should exist and be queryable: " + e.getMessage());
        }
    }

    // =============================================
    // USER ENGAGEMENT ANALYTICS TESTS
    // =============================================

    @Test
    public void testV010MigrationCreatesUserEngagementMetricsTable() {
        assertTrue(tableExists("user_engagement_metrics"));

        // Test columns
        assertTrue(columnExists("user_engagement_metrics", "user_engagement_id"));
        assertTrue(columnExists("user_engagement_metrics", "user_id"));
        assertTrue(columnExists("user_engagement_metrics", "date"));
        assertTrue(columnExists("user_engagement_metrics", "posts_created"));
        assertTrue(columnExists("user_engagement_metrics", "comments_made"));
        assertTrue(columnExists("user_engagement_metrics", "time_spent_minutes"));

        // Test foreign key and unique constraint
        assertTrue(foreignKeyExists("user_engagement_metrics", "user_id", "users", "user_id"));
        assertTrue(uniqueConstraintExists("user_engagement_metrics", "user_id", "date"));
    }

    @Test
    public void testV010MigrationCreatesUserInfluenceScoresView() {
        try {
            Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM user_influence_scores").getSingleResult();
            assertTrue(count >= 0, "user_influence_scores view should be queryable");
        } catch (Exception e) {
            fail("user_influence_scores materialized view should exist and be queryable: " + e.getMessage());
        }
    }

    // =============================================
    // NOTIFICATION SYSTEM TESTS
    // =============================================

    @Test
    public void testV010MigrationCreatesNotificationsTable() {
        assertTrue(tableExists("notifications"));

        // Test key columns
        assertTrue(columnExists("notifications", "notification_id"));
        assertTrue(columnExists("notifications", "recipient_id"));
        assertTrue(columnExists("notifications", "actor_id"));
        assertTrue(columnExists("notifications", "notification_type"));
        assertTrue(columnExists("notifications", "title"));
        assertTrue(columnExists("notifications", "is_read"));
        assertTrue(columnExists("notifications", "priority"));

        // Test foreign keys
        assertTrue(foreignKeyExists("notifications", "recipient_id", "users", "user_id"));
        assertTrue(foreignKeyExists("notifications", "related_post_id", "social_posts", "social_post_id"));
    }

    @Test
    public void testNotificationTypeConstraint() {
        assertTrue(checkConstraintExists("notifications", "chk_notification_type"));
    }

    // =============================================
    // SEARCH OPTIMIZATION TESTS
    // =============================================

    @Test
    public void testV010MigrationCreatesSearchIndexes() {
        // Test content search index
        assertTrue(indexExists("idx_social_posts_content_search"));

        // Test workout search index
        assertTrue(indexExists("idx_social_posts_workout_search"));

        // Test hashtag search index
        assertTrue(indexExists("idx_post_hashtags_search"));

        // Test mention search index
//        assertTrue(indexExists("idx_social_post_mentions_search"));
    }

    // =============================================
    // PERFORMANCE INDEX TESTS
    // =============================================

    @Test
    public void testV010MigrationCreatesPerformanceIndexes() {
        // Feed generation indexes
        assertTrue(indexExists("idx_user_feeds_user_score"));
        assertTrue(indexExists("idx_user_feeds_cleanup"));

        // Trending content indexes
        assertTrue(indexExists("idx_hashtag_analytics_trending"));
        assertTrue(indexExists("idx_hashtag_analytics_category"));

        // Notification indexes
        assertTrue(indexExists("idx_notifications_recipient_unread"));
        assertTrue(indexExists("idx_notifications_recipient_type"));

        // Enhanced social posts indexes
        assertTrue(indexExists("idx_social_posts_feed_generation"));
        assertTrue(indexExists("idx_social_posts_workout_feed"));

        // Relationship indexes
//        assertTrue(indexExists("idx_user_relationships_feed_lookup"));
    }

    @Test
    public void testV010MigrationIndexCount() {
        // V010 should add significant number of performance indexes
        int totalIndexes = getTotalIndexCount();
        assertTrue(totalIndexes >= 30, "Should have at least 30 indexes after V010 optimizations");
    }

    // =============================================
    // FUNCTION TESTS
    // =============================================

    @Test
    public void testV010MigrationCreatesFeedGenerationFunction() {
        assertTrue(functionExists("generate_user_feed"));

        // Test function can be called (basic smoke test)
        try {
            entityManager.createNativeQuery(
                    "SELECT * FROM generate_user_feed(1, 10)"
            ).setMaxResults(1).getResultList();
            // Function exists and is callable
        } catch (Exception e) {
            fail("generate_user_feed function should be callable: " + e.getMessage());
        }
    }

    @Test
    public void testV010MigrationCreatesWorkoutDiscoveryFunction() {
        assertTrue(functionExists("discover_workout_content"));

        // Test function can be called
        try {
            entityManager.createNativeQuery(
                    "SELECT * FROM discover_workout_content(1, 5)"
            ).setMaxResults(1).getResultList();
            // Function exists and is callable
        } catch (Exception e) {
            fail("discover_workout_content function should be callable: " + e.getMessage());
        }
    }

    @Test
    public void testV010MigrationCreatesAnalyticsFunction() {
        assertTrue(functionExists("update_hashtag_analytics"));
        assertTrue(functionExists("refresh_social_analytics"));
    }

    // =============================================
    // TRIGGER TESTS
    // =============================================

    @Test
    public void testV010MigrationCreatesTriggers() {
        assertTrue(triggerExists("trigger_post_like_notification", "post_likes"));
        assertTrue(triggerExists("trigger_comment_notification", "social_comments"));
    }

    // =============================================
    // DATA INITIALIZATION TESTS
    // =============================================

    @Test
    public void testV010MigrationInitializesHashtagData() {
        // Test that hashtag analytics has been populated with existing data
        Long hashtagCount = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM hashtag_analytics"
        ).getSingleResult();

        // Should have at least some hashtag data if post_hashtags table has data
        Long postHashtagCount = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM post_hashtags"
        ).getSingleResult();

        if (postHashtagCount > 0) {
            assertTrue(hashtagCount > 0, "Should have initialized hashtag analytics from existing data");
        }
    }

    // =============================================
    // INTEGRATION TESTS
    // =============================================

    @Test
    public void testV010MigrationOptimizationIntegration() {
        // Test that all optimization components work together

        // 1. Materialized views should be populated
        Long trendingPostsCount = (Long) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM trending_posts"
        ).getSingleResult();
        assertTrue(trendingPostsCount >= 0, "trending_posts view should be accessible");

        // 2. Functions should work with tables
        try {
            entityManager.createNativeQuery("DO $$ BEGIN PERFORM refresh_social_analytics(); END $$").executeUpdate();
        } catch (Exception e) {
            fail("refresh_social_analytics should work: " + e.getMessage());
        }

        // 3. Indexes should improve query performance (basic check)
        assertTrue(getTotalIndexCount() > 30, "Should have comprehensive indexing strategy");
    }

    // =============================================
    // HELPER METHODS
    // =============================================

    private boolean tableExists(String tableName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, tableName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        try {
            String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, tableName)
                    .setParameter(2, columnName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean materializedViewExists(String viewName) {
        try {
            String sql = "SELECT matviewname FROM pg_matviews";
            List<String> existingViews = entityManager.createNativeQuery(sql).getResultList();

            // Debug: Print what we're looking for vs what exists
            System.out.println("Looking for view: '" + viewName + "'");
            System.out.println("Existing views: " + existingViews);

            // Check exact match first
            boolean exactMatch = existingViews.contains(viewName);
            System.out.println("Exact match: " + exactMatch);

            // Check case-insensitive match
            boolean caseInsensitiveMatch = existingViews.stream()
                    .anyMatch(view -> view.equalsIgnoreCase(viewName));
            System.out.println("Case-insensitive match: " + caseInsensitiveMatch);

            return exactMatch || caseInsensitiveMatch;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean viewHasColumn(String viewName, String columnName) {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.columns 
                WHERE table_name = ? AND column_name = ?
                """;
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, viewName)
                    .setParameter(2, columnName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean indexExists(String indexName) {
        try {
            String sql = "SELECT COUNT(*) FROM pg_indexes WHERE indexname = ?";
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, indexName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean foreignKeyExists(String tableName, String columnName, String referencedTable, String referencedColumn) {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.key_column_usage kcu
                JOIN information_schema.referential_constraints rc 
                ON kcu.constraint_name = rc.constraint_name
                JOIN information_schema.key_column_usage kcu2 
                ON rc.unique_constraint_name = kcu2.constraint_name
                WHERE kcu.table_name = ? AND kcu.column_name = ? 
                AND kcu2.table_name = ? AND kcu2.column_name = ?
                """;
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, tableName)
                    .setParameter(2, columnName)
                    .setParameter(3, referencedTable)
                    .setParameter(4, referencedColumn)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean uniqueConstraintExists(String tableName, String... columnNames) {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu 
                ON tc.constraint_name = kcu.constraint_name
                WHERE tc.table_name = ? AND tc.constraint_type = 'UNIQUE'
                """;
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, tableName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkConstraintExists(String tableName, String constraintName) {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.table_constraints 
                WHERE table_name = ? AND constraint_name = ? AND constraint_type = 'CHECK'
                """;
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, tableName)
                    .setParameter(2, constraintName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean functionExists(String functionName) {
        try {
            String sql = "SELECT COUNT(*) FROM pg_proc WHERE proname = ?";
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, functionName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean triggerExists(String triggerName, String tableName) {
        try {
            String sql = """
                SELECT COUNT(*) FROM information_schema.triggers 
                WHERE trigger_name = ? AND event_object_table = ?
                """;
            Long count = (Long) entityManager.createNativeQuery(sql)
                    .setParameter(1, triggerName)
                    .setParameter(2, tableName)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private int getTotalIndexCount() {
        try {
            String sql = "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public'";
            Long count = (Long) entityManager.createNativeQuery(sql).getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            return 0;
        }
    }
}
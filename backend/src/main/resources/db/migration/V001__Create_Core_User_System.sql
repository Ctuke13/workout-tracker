-- =============================================================================
-- V001__Create_Core_User_System.sql
-- Creates users, subscriptions, and professional_profiles tables
-- EXACTLY MATCHES the JPA entity definitions with explicit column names
-- =============================================================================

-- =====================================================
-- USERS TABLE (matches User.java exactly)
-- =====================================================

CREATE TABLE users (
                       user_id BIGSERIAL PRIMARY KEY,

    -- Basic account info
                       username VARCHAR(20) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,  -- Note: NOT password_hash!

    -- Personal info
                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255) NOT NULL,
                       date_of_birth DATE,
                       gender VARCHAR(20),

    -- Location
                       zipcode VARCHAR(255),
                       city VARCHAR(255),
                       state VARCHAR(255),
                       country VARCHAR(255) DEFAULT 'US',
                       phone_number VARCHAR(255),

    -- Profile
                       profile_image_url VARCHAR(255),
                       bio VARCHAR(500),

    -- User classification
                       user_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR',
                       subscription_tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
                       account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                       privacy_settings VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
                       notification_settings VARCHAR(20) NOT NULL DEFAULT 'ALL',
                       measurement_system VARCHAR(20) NOT NULL DEFAULT 'METRIC',
                       activity_level VARCHAR(30),

    -- Fitness Information
                       fitness_level VARCHAR(20),
                       height_cm INTEGER,
                       weight_kg DOUBLE PRECISION,
                       workout_frequency VARCHAR(30),
                       fitness_goals VARCHAR(1000),

    -- Activity Tracking
                       last_active TIMESTAMP,
                       total_workouts INTEGER DEFAULT 0,
                       current_streak INTEGER DEFAULT 0,
                       longest_streak INTEGER DEFAULT 0,

    -- Account Management (renamed to avoid UserDetails conflicts)
                       email_verified BOOLEAN DEFAULT false,
                       enabled BOOLEAN DEFAULT true,
                       account_non_expired BOOLEAN DEFAULT true,
                       account_non_locked BOOLEAN DEFAULT true,
                       credentials_non_expired BOOLEAN DEFAULT true,

    -- Timestamps
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add constraints for User enum values
ALTER TABLE users ADD CONSTRAINT chk_user_type
    CHECK (user_type IN ('REGULAR', 'PROFESSIONAL', 'ADMIN'));

ALTER TABLE users ADD CONSTRAINT chk_user_subscription_tier
    CHECK (subscription_tier IN ('FREE', 'PLUS', 'PRO', 'PRO_PROFESSIONAL'));

ALTER TABLE users ADD CONSTRAINT chk_account_status
    CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'PENDING_VERIFICATION'));

ALTER TABLE users ADD CONSTRAINT chk_gender
    CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY'));

ALTER TABLE users ADD CONSTRAINT chk_privacy_settings
    CHECK (privacy_settings IN ('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE'));

ALTER TABLE users ADD CONSTRAINT chk_notification_settings
    CHECK (notification_settings IN ('ALL', 'WORKOUT_ONLY', 'SOCIAL_ONLY', 'NONE'));

ALTER TABLE users ADD CONSTRAINT chk_measurement_system
    CHECK (measurement_system IN ('METRIC', 'IMPERIAL'));

ALTER TABLE users ADD CONSTRAINT chk_activity_level
    CHECK (activity_level IN ('SEDENTARY', 'LIGHTLY_ACTIVE', 'MODERATELY_ACTIVE', 'VERY_ACTIVE', 'EXTREMELY_ACTIVE'));

ALTER TABLE users ADD CONSTRAINT chk_fitness_level
    CHECK (fitness_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'));

ALTER TABLE users ADD CONSTRAINT chk_workout_frequency
    CHECK (workout_frequency IN ('RARELY', 'ONCE_WEEK', 'TWICE_WEEK', 'REGULARLY', 'DAILY', 'MULTIPLE_DAILY'));

-- =====================================================
-- SUBSCRIPTIONS TABLE (matches Subscription.java exactly)
-- =====================================================

CREATE TABLE subscriptions (
                               subscription_id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL,

    -- Subscription details
                               subscription_tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
                               status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    -- Dates
                               start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               end_date TIMESTAMP,
                               next_billing_date TIMESTAMP,

    -- Stripe Integration
                               stripe_subscription_id VARCHAR(255) UNIQUE,
                               stripe_customer_id VARCHAR(255),

    -- Additional fields
                               auto_renew BOOLEAN DEFAULT true,
                               cancellation_reason VARCHAR(255),
                               cancelled_at TIMESTAMP,

    -- Timestamps
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key
                               CONSTRAINT fk_subscription_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Add constraints for Subscription enum values
-- Drop constraint if it exists to avoid Flyway/H2 test failure
ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS chk_subscription_tier;

-- Recreate enum constraint safely
ALTER TABLE subscriptions ADD CONSTRAINT chk_subscription_tier
    CHECK (subscription_tier IN ('FREE', 'PLUS', 'PRO', 'PRO_PROFESSIONAL'));

ALTER TABLE subscriptions ADD CONSTRAINT chk_status
    CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED', 'PENDING'));

-- =====================================================
-- PROFESSIONAL_PROFILES TABLE (matches ProfessionalProfile.java exactly)
-- =====================================================

CREATE TABLE professional_profiles (
                                       professional_profile_id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT NOT NULL,

    -- Professional Identity
                                       display_name VARCHAR(100),
                                       business_name VARCHAR(100),
                                       bio TEXT,
                                       service_type VARCHAR(50) NOT NULL DEFAULT 'PERSONAL_TRAINER',

    -- Service Details
                                       years_experience INTEGER DEFAULT 0,
                                       experience_level VARCHAR(20) DEFAULT 'BEGINNER',
                                       hourly_rate DOUBLE PRECISION,
                                       base_zipcode VARCHAR(5),
                                       service_area_selection_method VARCHAR(30) DEFAULT 'CITY_BASED',
                                       max_travel_miles INTEGER DEFAULT 15,

    -- Service Options
                                       offers_virtual_sessions BOOLEAN DEFAULT false,
                                       offers_in_home_sessions BOOLEAN DEFAULT false,
                                       offers_gym_sessions BOOLEAN DEFAULT true,
                                       offers_group_sessions BOOLEAN DEFAULT false,
                                       accepts_package_deals BOOLEAN DEFAULT false,

    -- Professional Credentials
                                       website_url VARCHAR(500),
                                       license_number VARCHAR(50),

    -- Availability & Scheduling
                                       availability_pattern VARCHAR(30) DEFAULT 'FLEXIBLE',
                                       typical_availability VARCHAR(500),
                                       booking_lead_time_hours INTEGER DEFAULT 24,
                                       session_duration_minutes INTEGER DEFAULT 60,

    -- Client Management
                                       accepts_new_clients BOOLEAN DEFAULT true,
                                       max_clients INTEGER DEFAULT 20,
                                       min_client_age INTEGER DEFAULT 16,
                                       max_client_age INTEGER DEFAULT 80,
                                       preferred_contact_method VARCHAR(30) DEFAULT 'PLATFORM_MESSAGE',
                                       response_time_hours INTEGER DEFAULT 4,

    -- Verification & Trust
                                       is_verified BOOLEAN DEFAULT false,
                                       verification_status VARCHAR(20) DEFAULT 'PENDING',
                                       verification_submitted_at TIMESTAMP,
                                       verified_at TIMESTAMP,
                                       verification_reviewed_at TIMESTAMP,
                                       verification_notes VARCHAR(1000),
                                       has_liability_insurance BOOLEAN DEFAULT false,
                                       insurance_expiry_date TIMESTAMP,
                                       background_check_completed BOOLEAN DEFAULT false,
                                       background_check_date TIMESTAMP,

    -- Performance Metrics
                                       total_clients_served INTEGER DEFAULT 0,
                                       active_clients_count INTEGER DEFAULT 0,
                                       total_sessions_completed INTEGER DEFAULT 0,
                                       average_rating DOUBLE PRECISION DEFAULT 0.0,
                                       total_reviews INTEGER DEFAULT 0,
                                       profile_views INTEGER DEFAULT 0,

    -- Business Settings
                                       cancellation_policy VARCHAR(1000),
                                       payment_terms VARCHAR(500),

    -- Profile Settings
                                       profile_completion_percentage INTEGER DEFAULT 0,
                                       is_profile_public BOOLEAN DEFAULT true,
                                       featured_until TIMESTAMP,
                                       subscription_tier_required VARCHAR(20) DEFAULT 'PLUS',

    -- Timestamps
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key
                                       CONSTRAINT fk_professional_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Add constraints for ProfessionalProfile enum values
ALTER TABLE professional_profiles ADD CONSTRAINT chk_service_type
    CHECK (service_type IN ('PERSONAL_TRAINER', 'NUTRITIONIST', 'YOGA_INSTRUCTOR', 'PILATES_INSTRUCTOR', 'PHYSICAL_THERAPIST', 'WELLNESS_COACH', 'STRENGTH_COACH', 'SPORTS_COACH', 'FITNESS_INSTRUCTOR', 'OTHER'));

ALTER TABLE professional_profiles ADD CONSTRAINT chk_experience_level
    CHECK (experience_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'));

ALTER TABLE professional_profiles ADD CONSTRAINT chk_service_area_selection_method
    CHECK (service_area_selection_method IN ('CITY_BASED', 'DISTANCE_BASED', 'MANUAL_ZIPCODE'));

ALTER TABLE professional_profiles ADD CONSTRAINT chk_availability_pattern
    CHECK (availability_pattern IN ('FULL_TIME', 'PART_TIME', 'EVENINGS_WEEKENDS', 'MORNINGS_ONLY', 'FLEXIBLE', 'BY_APPOINTMENT'));

ALTER TABLE professional_profiles ADD CONSTRAINT chk_preferred_contact_method
    CHECK (preferred_contact_method IN ('PLATFORM_MESSAGE', 'EMAIL', 'PHONE', 'TEXT_MESSAGE', 'VIDEO_CALL', 'IN_PERSON'));

ALTER TABLE professional_profiles ADD CONSTRAINT chk_verification_status
    CHECK (verification_status IN ('NOT_SUBMITTED', 'PENDING', 'UNDER_REVIEW', 'VERIFIED', 'REJECTED', 'EXPIRED'));

ALTER TABLE professional_profiles ADD CONSTRAINT chk_rating
    CHECK (average_rating >= 0.0 AND average_rating <= 5.0);

-- =====================================================
-- COLLECTION TABLES FOR PROFESSIONAL_PROFILES LISTS
-- =====================================================

-- Specializations list
CREATE TABLE professional_specializations (
                                              professional_profile_id BIGINT NOT NULL,
                                              specialization VARCHAR(255) NOT NULL,
                                              CONSTRAINT fk_specializations_professional FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(professional_profile_id) ON DELETE CASCADE
);

-- Certifications list
CREATE TABLE professional_certifications (
                                             professional_profile_id BIGINT NOT NULL,
                                             certification VARCHAR(255) NOT NULL,
                                             CONSTRAINT fk_certifications_professional FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(professional_profile_id) ON DELETE CASCADE
);

-- Service areas list
CREATE TABLE professional_service_areas (
                                            professional_profile_id BIGINT NOT NULL,
                                            zipcode VARCHAR(255) NOT NULL,
                                            CONSTRAINT fk_service_areas_professional FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(professional_profile_id) ON DELETE CASCADE
);

-- Social media links list
CREATE TABLE professional_social_links (
                                           professional_profile_id BIGINT NOT NULL,
                                           social_link VARCHAR(255) NOT NULL,
                                           CONSTRAINT fk_social_links_professional FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(professional_profile_id) ON DELETE CASCADE
);

-- =====================================================
-- INDEXES FOR PERFORMANCE (Based on Repository Analysis)
-- =====================================================

-- Users table indexes
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_subscription_tier ON users(subscription_tier);
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_account_status ON users(account_status);
CREATE INDEX idx_users_last_active ON users(last_active);
CREATE INDEX idx_users_current_streak ON users(current_streak);
CREATE INDEX idx_users_longest_streak ON users(longest_streak);
CREATE INDEX idx_users_total_workouts ON users(total_workouts);
CREATE INDEX idx_users_location ON users(city, state, country);
CREATE INDEX idx_users_zipcode ON users(zipcode);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_users_email_verified ON users(email_verified);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Subscriptions table indexes
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_tier ON subscriptions(subscription_tier);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_dates ON subscriptions(start_date, end_date);
CREATE INDEX idx_subscriptions_next_billing ON subscriptions(next_billing_date);
CREATE INDEX idx_subscriptions_stripe_subscription ON subscriptions(stripe_subscription_id);
CREATE INDEX idx_subscriptions_stripe_customer ON subscriptions(stripe_customer_id);
CREATE INDEX idx_subscriptions_auto_renew ON subscriptions(auto_renew, next_billing_date);

-- Professional profiles indexes
CREATE INDEX idx_professional_user_id ON professional_profiles(user_id);
CREATE INDEX idx_professional_service_type ON professional_profiles(service_type);
CREATE INDEX idx_professional_verification_status ON professional_profiles(verification_status);
CREATE INDEX idx_professional_verified_at ON professional_profiles(verified_at);
CREATE INDEX idx_professional_hourly_rate ON professional_profiles(hourly_rate);
CREATE INDEX idx_professional_rating ON professional_profiles(average_rating);
CREATE INDEX idx_professional_total_reviews ON professional_profiles(total_reviews);
CREATE INDEX idx_professional_accepts_clients ON professional_profiles(accepts_new_clients);
CREATE INDEX idx_professional_virtual_sessions ON professional_profiles(offers_virtual_sessions);
CREATE INDEX idx_professional_base_zipcode ON professional_profiles(base_zipcode);
CREATE INDEX idx_professional_profile_views ON professional_profiles(profile_views);
CREATE INDEX idx_professional_created_at ON professional_profiles(created_at);

-- Composite indexes for complex queries
CREATE INDEX idx_professional_search ON professional_profiles(service_type, verification_status, accepts_new_clients);
CREATE INDEX idx_professional_location_service ON professional_profiles(service_type, offers_virtual_sessions, offers_in_home_sessions);
CREATE INDEX idx_users_subscription_active ON users(subscription_tier, enabled);

-- Collection table indexes
CREATE INDEX idx_specializations_professional ON professional_specializations(professional_profile_id);
CREATE INDEX idx_certifications_professional ON professional_certifications(professional_profile_id);
CREATE INDEX idx_service_areas_professional ON professional_service_areas(professional_profile_id);
CREATE INDEX idx_service_areas_zipcode ON professional_service_areas(zipcode);
CREATE INDEX idx_social_links_professional ON professional_social_links(professional_profile_id);

-- =====================================================
-- UNIQUE CONSTRAINTS
-- =====================================================

-- Ensure one subscription per user
CREATE UNIQUE INDEX idx_unique_user_subscription ON subscriptions(user_id);

-- Ensure one professional profile per user
CREATE UNIQUE INDEX idx_unique_user_professional ON professional_profiles(user_id);
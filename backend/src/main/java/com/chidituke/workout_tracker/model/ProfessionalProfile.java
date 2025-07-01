package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Professional Profile entity for fitness professionals
 * Represents business information, services, and professional capabilities
 */
@Data
@Entity
@Table(name = "professional_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
public class ProfessionalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "professional_profile_id")
    private Long id;

    // ==================== CORE RELATIONSHIP ====================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ==================== PROFESSIONAL IDENTITY ====================

    // ✅ ADDED: Missing displayName field
    @Column(name = "display_name", length = 100)
    @Size(max = 100, message = "Display name cannot exceed 100 characters")
    private String displayName;

    @Column(name = "business_name", length = 100)
    @Size(max = 100, message = "Business name cannot exceed 100 characters")
    private String businessName;

    @Column(name = "bio", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    private ServiceType serviceType = ServiceType.PERSONAL_TRAINER;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_specializations", joinColumns = @JoinColumn(name = "professional_profile_id"))
    @Column(name = "specialization")
    @Size(max = 10, message = "Maximum 10 specializations allowed")
    @Builder.Default
    private List<String> specializations = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_certifications", joinColumns = @JoinColumn(name = "professional_profile_id"))
    @Column(name = "certification")
    @Size(max = 15, message = "Maximum 15 certifications allowed")
    @Builder.Default
    private List<String> certifications = new ArrayList<>();

    // ==================== SERVICE DETAILS ====================

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience cannot exceed 50")
    @Column(name = "years_experience")
    @Builder.Default
    private Integer yearsExperience = 0;

    // ✅ ADDED: Missing experienceLevel field
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level")
    @Builder.Default
    private ExperienceLevel experienceLevel = ExperienceLevel.BEGINNER;

    @DecimalMin(value = "0.0", message = "Hourly rate cannot be negative")
    @DecimalMax(value = "1000.0", message = "Hourly rate cannot exceed $1000")
    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Pattern(regexp = "^\\d{5}$", message = "Base zipcode must be 5 digits")
    @Column(name = "base_zipcode")
    private String baseZipcode;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_service_areas", joinColumns = @JoinColumn(name = "professional_profile_id"))
    @Column(name = "zipcode")
    @Size(max = 50, message = "Maximum 50 service areas allowed")
    @Builder.Default
    private List<String> serviceAreas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "service_area_selection_method")
    @Builder.Default
    private ServiceAreaSelectionMethod selectionMethod = ServiceAreaSelectionMethod.CITY_BASED;

    @Min(value = 0, message = "Travel distance cannot be negative")
    @Max(value = 500, message = "Travel distance cannot exceed 500 miles")
    @Column(name = "max_travel_miles")
    @Builder.Default
    private Integer maxTravelMiles = 15;

    // ==================== SERVICE OPTIONS ====================

    @Column(name = "offers_virtual_sessions")
    @Builder.Default
    private Boolean offersVirtualSessions = false;

    @Column(name = "offers_in_home_sessions")
    @Builder.Default
    private Boolean offersInHomeService = false;

    @Column(name = "offers_gym_sessions")
    @Builder.Default
    private Boolean offersGymSessions = true;

    @Column(name = "offers_group_sessions")
    @Builder.Default
    private Boolean offersGroupSessions = false;

    @Column(name = "accepts_package_deals")
    @Builder.Default
    private Boolean acceptsPackageDeals = false;

    // ==================== PROFESSIONAL CREDENTIALS ====================

    @Column(name = "website_url")
    @Size(max = 500, message = "Website URL cannot exceed 500 characters")
    private String websiteUrl;

    @Column(name = "license_number")
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    private String licenseNumber;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_social_links", joinColumns = @JoinColumn(name = "professional_profile_id"))
    @Column(name = "social_link")
    @Size(max = 10, message = "Maximum 10 social media links allowed")
    @Builder.Default
    private List<String> socialMediaLinks = new ArrayList<>();

    // ==================== AVAILABILITY & SCHEDULING ====================

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_pattern")
    @Builder.Default
    private AvailabilityPattern availabilityPattern = AvailabilityPattern.FLEXIBLE;

    @Column(name = "typical_availability", length = 500)
    @Size(max = 500, message = "Availability description cannot exceed 500 characters")
    private String typicalAvailability;

    @Min(value = 0, message = "Booking lead time cannot be negative")
    @Max(value = 168, message = "Booking lead time cannot exceed 1 week")
    @Column(name = "booking_lead_time_hours")
    @Builder.Default
    private Integer bookingLeadTimeHours = 24;

    @Min(value = 15, message = "Session duration must be at least 15 minutes")
    @Max(value = 480, message = "Session duration cannot exceed 8 hours")
    @Column(name = "session_duration_minutes")
    @Builder.Default
    private Integer sessionDurationMinutes = 60;

    // ==================== CLIENT MANAGEMENT ====================

    @Column(name = "accepts_new_clients")
    @Builder.Default
    private Boolean acceptsNewClients = true;

    @Min(value = 1, message = "Maximum clients must be at least 1")
    @Max(value = 500, message = "Maximum clients cannot exceed 500")
    @Column(name = "max_clients")
    @Builder.Default
    private Integer maxClients = 20;

    @Min(value = 13, message = "Minimum client age cannot be less than 13")
    @Max(value = 120, message = "Minimum client age cannot exceed 120")
    @Column(name = "min_client_age")
    @Builder.Default
    private Integer minClientAge = 16;

    @Min(value = 16, message = "Maximum client age cannot be less than 16")
    @Max(value = 120, message = "Maximum client age cannot exceed 120")
    @Column(name = "max_client_age")
    @Builder.Default
    private Integer maxClientAge = 80;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method")
    @Builder.Default
    private ContactMethod preferredContactMethod = ContactMethod.PLATFORM_MESSAGE;

    @Min(value = 0, message = "Response time cannot be negative")
    @Max(value = 168, message = "Response time cannot exceed 1 week")
    @Column(name = "response_time_hours")
    @Builder.Default
    private Integer responseTimeHours = 4;

    // ==================== VERIFICATION & TRUST ====================

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    // ✅ ADDED: Missing verificationStatus field
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verification_submitted_at")
    private LocalDateTime verificationSubmittedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // ✅ ADDED: Missing verificationReviewedAt field
    @Column(name = "verification_reviewed_at")
    private LocalDateTime verificationReviewedAt;

    @Column(name = "verification_notes", length = 1000)
    @Size(max = 1000, message = "Verification notes cannot exceed 1000 characters")
    private String verificationNotes;

    @Column(name = "has_liability_insurance")
    @Builder.Default
    private Boolean hasLiabilityInsurance = false;

    @Column(name = "insurance_expiry_date")
    private LocalDateTime insuranceExpiryDate;

    @Column(name = "background_check_completed")
    @Builder.Default
    private Boolean backgroundCheckCompleted = false;

    @Column(name = "background_check_date")
    private LocalDateTime backgroundCheckDate;

    // ==================== PERFORMANCE METRICS ====================

    @Column(name = "total_clients_served")
    @Builder.Default
    private Integer totalClientsServed = 0;

    @Column(name = "active_clients_count")
    @Builder.Default
    private Integer activeClientsCount = 0;

    @Column(name = "total_sessions_completed")
    @Builder.Default
    private Integer totalSessionsCompleted = 0;

    @DecimalMin(value = "0.0", message = "Average rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Average rating cannot exceed 5.0")
    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Min(value = 0, message = "Total reviews cannot be negative")
    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Min(value = 0, message = "Profile views cannot be negative")
    @Column(name = "profile_views")
    @Builder.Default
    private Integer profileViews = 0;

    // ==================== BUSINESS SETTINGS ====================

    @Column(name = "cancellation_policy", length = 1000)
    @Size(max = 1000, message = "Cancellation policy cannot exceed 1000 characters")
    private String cancellationPolicy;

    @Column(name = "payment_terms", length = 500)
    @Size(max = 500, message = "Payment terms cannot exceed 500 characters")
    private String paymentTerms;

    // ==================== PROFILE SETTINGS ====================

    @Min(value = 0, message = "Profile completion cannot be negative")
    @Max(value = 100, message = "Profile completion cannot exceed 100")
    @Column(name = "profile_completion_percentage")
    @Builder.Default
    private Integer profileCompletionPercentage = 0;

    @Column(name = "is_profile_public")
    @Builder.Default
    private Boolean isProfilePublic = true;

    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;

    @Column(name = "subscription_tier_required")
    @Builder.Default
    private String subscriptionTierRequired = "PLUS";

    // ==================== TIMESTAMPS ====================

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ==================== LOCATION METHODS ====================

    /**
     * ✅ FIXED: Added missing method for User.getBusinessLocation()
     */
    public String getBusinessLocationDisplay() {
        if (baseZipcode != null && !baseZipcode.trim().isEmpty()) {
            return baseZipcode;
        }
        return "Location not set";
    }

    /**
     * Get full business location with service areas
     */
    public String getFullBusinessLocation() {
        StringBuilder location = new StringBuilder();

        if (baseZipcode != null && !baseZipcode.trim().isEmpty()) {
            location.append("Based in ").append(baseZipcode);
        }

        if (serviceAreas != null && !serviceAreas.isEmpty()) {
            if (location.length() > 0) location.append(" | ");
            location.append("Serves ").append(serviceAreas.size()).append(" areas");
        }

        if (offersVirtualSessions) {
            if (location.length() > 0) location.append(" | ");
            location.append("Virtual sessions available");
        }

        return location.length() > 0 ? location.toString() : "Location not specified";
    }

    /**
     * Check if professional can serve a specific location
     */
    public boolean canServeLocation(String zipcode) {
        return offersVirtualSessions ||
                (serviceAreas != null && serviceAreas.contains(zipcode));
    }

    // ==================== ACTIVITY STATUS METHODS ====================

    /**
     * ✅ FIXED: Check if professional is currently active (last 15 minutes)
     */
    public boolean isProfessionalCurrentlyActive() {
        if (user == null || user.getLastActive() == null) return false;
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        return user.getLastActive().isAfter(cutoff);
    }

    /**
     * ✅ FIXED: Check if professional was active today
     */
    public boolean isProfessionalActiveToday() {
        if (user == null || user.getLastActive() == null) return false;
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return user.getLastActive().isAfter(todayStart);
    }

    /**
     * ✅ FIXED: Get activity status for specific viewer (privacy-aware)
     */
    public String getProfessionalActivityStatusForViewer(User viewer) {
        if (user == null) return "Unknown";

        // Check privacy settings
        if (user.getPrivacySettings() == User.PrivacySettings.PRIVATE &&
                (viewer == null || !viewer.getId().equals(user.getId()))) {
            return "Private";
        }

        if (isProfessionalCurrentlyActive()) {
            return "Active now";
        } else if (isProfessionalActiveToday()) {
            return "Active today";
        } else if (user.getLastActive() != null &&
                user.getLastActive().isAfter(LocalDateTime.now().minusDays(7))) {
            return "Active this week";
        } else {
            return "Inactive";
        }
    }

    /**
     * Get public activity status
     */
    public String getProfessionalActivityStatus() {
        return getProfessionalActivityStatusForViewer(null);
    }

    /**
     * ✅ FIXED: Get professional's activity level
     */
    public User.ActivityLevel getProfessionalActivityLevel() {
        return user != null ? user.getActivityLevel() : User.ActivityLevel.SEDENTARY;
    }

    /**
     * ✅ FIXED: Get activity boost score using correct enum values
     */
    public int getActivityBoostScore() {
        if (user == null) return 0;

        User.ActivityLevel level = user.getActivityLevel();
        if (level == null) return 20;

        return switch (level) {
            case EXTREMELY_ACTIVE -> 100;  // Most active professionals
            case VERY_ACTIVE -> 80;        // Very active professionals
            case MODERATELY_ACTIVE -> 60;  // Moderately active professionals
            case LIGHTLY_ACTIVE -> 40;     // Lightly active professionals
            case SEDENTARY -> 20;          // Least active professionals
        };
    }

    // ==================== AVAILABILITY & CLIENT MANAGEMENT ====================

    /**
     * Check if professional is accepting new clients
     */
    public boolean isAcceptingClients() {
        return acceptsNewClients &&
                (maxClients == null || activeClientsCount < maxClients) &&
                isProfessionalActiveToday();
    }

    /**
     * Check if professional is actively accepting clients (currently active)
     */
    public boolean isActivelyAcceptingClients() {
        return isAcceptingClients() && isProfessionalCurrentlyActive();
    }

    /**
     * Get current availability status
     */
    public String getAvailabilityStatus() {
        if (!acceptsNewClients) {
            return "Not accepting new clients";
        }

        if (maxClients != null && activeClientsCount >= maxClients) {
            return "Fully booked";
        }

        if (isProfessionalCurrentlyActive()) {
            return "Available now";
        }

        if (isProfessionalActiveToday()) {
            return "Available today";
        }

        return "Available for booking";
    }

    /**
     * Get expected response time based on activity
     */
    public String getExpectedResponseTime() {
        if (user == null || responseTimeHours == null) {
            return "Response time not specified";
        }

        if (isProfessionalCurrentlyActive()) {
            return "Active now - quick response expected";
        }

        if (isProfessionalActiveToday()) {
            return "Within " + Math.min(responseTimeHours, 2) + " hours";
        }

        return "Within " + responseTimeHours + " hours";
    }

    /**
     * Check if professional is a quick responder
     */
    public boolean isQuickResponder() {
        return isProfessionalCurrentlyActive() ||
                (isProfessionalActiveToday() && responseTimeHours != null && responseTimeHours <= 4);
    }

    // ==================== VERIFICATION & TRUST ====================

    /**
     * Check if professional is fully verified
     */
    public boolean isFullyVerified() {
        return Boolean.TRUE.equals(isVerified) &&
                Boolean.TRUE.equals(hasLiabilityInsurance) &&
                Boolean.TRUE.equals(backgroundCheckCompleted);
    }

    /**
     * Get verification status as string
     */
    public String getVerificationStatusString() {
        if (isFullyVerified()) return "FULLY_VERIFIED";
        if (Boolean.TRUE.equals(isVerified)) return "PARTIALLY_VERIFIED";
        if (verificationSubmittedAt != null) return "PENDING_VERIFICATION";
        return "UNVERIFIED";
    }

    /**
     * Check if user needs subscription upgrade for professional features
     */
    public boolean requiresSubscriptionUpgrade(String userSubscriptionTier) {
        if (subscriptionTierRequired == null) return false;

        return switch (userSubscriptionTier.toUpperCase()) {
            case "FREE" -> !subscriptionTierRequired.equals("FREE");
            case "PLUS" -> subscriptionTierRequired.equals("PRO") || subscriptionTierRequired.equals("PRO_PROFESSIONAL");
            case "PRO" -> subscriptionTierRequired.equals("PRO_PROFESSIONAL");
            case "PRO_PROFESSIONAL" -> false;
            default -> true;
        };
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * ✅ UPDATED: Get display name (displayName field, business name, or user's full name)
     */
    public String getDisplayName() {
        // First priority: explicit displayName field
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }

        // Second priority: business name
        if (businessName != null && !businessName.trim().isEmpty()) {
            return businessName;
        }

        // Third priority: user's full name
        if (user != null) {
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            String fullName = (firstName + " " + lastName).trim();
            return !fullName.isEmpty() ? fullName : "Professional";
        }

        return "Professional Profile";
    }

    /**
     * Get display name with service type
     */
    public String getDisplayNameWithServiceType() {
        String name = getDisplayName();
        String serviceTypeName = serviceType != null ? serviceType.getDisplayName() : "Fitness Professional";
        return name + " - " + serviceTypeName;
    }

    /**
     * Increment profile views counter
     */
    public void incrementProfileViews() {
        this.profileViews = (this.profileViews == null ? 0 : this.profileViews) + 1;
    }

    /**
     * Update last active timestamp (delegates to User)
     */
    public void updateLastActive() {
        if (user != null) {
            user.updateActivity();
        }
    }

    /**
     * Add completed session to metrics
     */
    public void addCompletedSession() {
        this.totalSessionsCompleted = (this.totalSessionsCompleted == null ? 0 : this.totalSessionsCompleted) + 1;
    }

    /**
     * Update rating with new review
     */
    public void updateRating(double newRating) {
        if (newRating < 0.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }

        if (this.totalReviews == null) this.totalReviews = 0;
        if (this.averageRating == null) this.averageRating = 0.0;

        double totalRatingPoints = this.averageRating * this.totalReviews;
        this.totalReviews++;
        this.averageRating = (totalRatingPoints + newRating) / this.totalReviews;

        // Round to 2 decimal places
        this.averageRating = Math.round(this.averageRating * 100.0) / 100.0;
    }

    public Double calculateProfileCompletion() {
        int completedFields = 0;
        int totalFields = 15;

        if (bio != null && !bio.trim().isEmpty()) completedFields++;
        if (specializations != null && !specializations.isEmpty()) completedFields++;
        if (certifications != null && !certifications.isEmpty()) completedFields++;
        if (yearsExperience != null && yearsExperience > 0) completedFields++;
        if (hourlyRate != null && hourlyRate > 0) completedFields++;
        if (baseZipcode != null && !baseZipcode.trim().isEmpty()) completedFields++;
        if (serviceAreas != null && !serviceAreas.isEmpty()) completedFields++;
        if (typicalAvailability != null && !typicalAvailability.trim().isEmpty()) completedFields++;
        if (sessionDurationMinutes != null) completedFields++;
        if (minClientAge != null) completedFields++;
        if (maxClientAge != null) completedFields++;
        if (cancellationPolicy != null && !cancellationPolicy.trim().isEmpty()) completedFields++;
        if (paymentTerms != null && !paymentTerms.trim().isEmpty()) completedFields++;
        if (Boolean.TRUE.equals(hasLiabilityInsurance)) completedFields++;
        if (Boolean.TRUE.equals(backgroundCheckCompleted)) completedFields++;

        double percentage = ((double) completedFields * 100) / totalFields;
        this.profileCompletionPercentage = (int) Math.round(percentage);
        return percentage;
    }

    // ==================== JPA LIFECYCLE METHODS ====================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateProfileCompletion();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateProfileCompletion();
    }

    // ==================== ENUMS ====================

    public enum ServiceType {
        PERSONAL_TRAINER("Personal Trainer", "One-on-one fitness training and coaching"),
        NUTRITIONIST("Nutritionist", "Dietary planning and nutritional counseling"),
        YOGA_INSTRUCTOR("Yoga Instructor", "Yoga classes and mindfulness training"),
        PILATES_INSTRUCTOR("Pilates Instructor", "Pilates classes and movement therapy"),
        PHYSICAL_THERAPIST("Physical Therapist", "Rehabilitation and injury recovery"),
        WELLNESS_COACH("Wellness Coach", "Holistic health and lifestyle coaching"),
        STRENGTH_COACH("Strength Coach", "Specialized strength and conditioning training"),
        SPORTS_COACH("Sports Coach", "Sport-specific training and performance coaching"),
        FITNESS_INSTRUCTOR("Fitness Instructor", "Group fitness classes and programs"),
        OTHER("Other", "Other professional fitness services");

        private final String displayName;
        private final String description;

        ServiceType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Check if this service type requires specific certification
         */
        public boolean requiresCertification() {
            return this == PERSONAL_TRAINER ||
                    this == NUTRITIONIST ||
                    this == PHYSICAL_THERAPIST ||
                    this == STRENGTH_COACH;
        }

        /**
         * Get related specializations for this service type
         */
        public String[] getCommonSpecializations() {
            return switch (this) {
                case PERSONAL_TRAINER -> new String[]{
                        "Weight Loss", "Muscle Building", "Functional Training",
                        "HIIT", "Strength Training", "Cardio Training"
                };
                case NUTRITIONIST -> new String[]{
                        "Weight Management", "Sports Nutrition", "Meal Planning",
                        "Dietary Restrictions", "Metabolic Health"
                };
                case YOGA_INSTRUCTOR -> new String[]{
                        "Hatha Yoga", "Vinyasa Flow", "Yin Yoga",
                        "Hot Yoga", "Meditation", "Breathwork"
                };
                case PILATES_INSTRUCTOR -> new String[]{
                        "Mat Pilates", "Reformer Pilates", "Classical Pilates",
                        "Contemporary Pilates", "Rehabilitation Pilates"
                };
                case PHYSICAL_THERAPIST -> new String[]{
                        "Injury Rehabilitation", "Sports Medicine", "Manual Therapy",
                        "Movement Analysis", "Pain Management"
                };
                case WELLNESS_COACH -> new String[]{
                        "Stress Management", "Lifestyle Design", "Habit Formation",
                        "Goal Setting", "Mindfulness", "Work-Life Balance"
                };
                default -> new String[]{"General Fitness", "Health Coaching"};
            };
        }
    }

    public enum ExperienceLevel {
        BEGINNER("Beginner", "0-2 years", "New to the profession with basic qualifications"),
        INTERMEDIATE("Intermediate", "3-5 years", "Several years of experience with proven track record"),
        ADVANCED("Advanced", "6-10 years", "Extensive experience with specialized skills"),
        EXPERT("Expert", "10+ years", "Industry expert with comprehensive expertise");

        private final String displayName;
        private final String yearRange;
        private final String description;

        ExperienceLevel(String displayName, String yearRange, String description) {
            this.displayName = displayName;
            this.yearRange = yearRange;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getYearRange() {
            return yearRange;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Get full display with year range
         */
        public String getFullDisplay() {
            return displayName + " (" + yearRange + ")";
        }

        /**
         * Get experience level based on years
         */
        public static ExperienceLevel fromYears(int years) {
            if (years <= 2) return BEGINNER;
            if (years <= 5) return INTERMEDIATE;
            if (years <= 10) return ADVANCED;
            return EXPERT;
        }

        /**
         * Get minimum years for this level
         */
        public int getMinimumYears() {
            return switch (this) {
                case BEGINNER -> 0;
                case INTERMEDIATE -> 3;
                case ADVANCED -> 6;
                case EXPERT -> 10;
            };
        }
    }

    // ✅ ADDED: Missing VerificationStatus enum
    public enum VerificationStatus {
        NOT_SUBMITTED("Not Submitted", "Verification documents have not been submitted"),
        PENDING("Pending", "Verification documents submitted, waiting for review"),
        UNDER_REVIEW("Under Review", "Verification documents are being reviewed by admin"),
        VERIFIED("Verified", "Professional has been successfully verified"),
        REJECTED("Rejected", "Verification was rejected, resubmission required"),
        EXPIRED("Expired", "Verification has expired and needs renewal");

        private final String displayName;
        private final String description;

        VerificationStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Check if this status represents a verified state
         */
        public boolean isVerified() {
            return this == VERIFIED;
        }

        /**
         * Check if this status requires action
         */
        public boolean requiresAction() {
            return this == REJECTED || this == EXPIRED;
        }

        /**
         * Check if this status is pending review
         */
        public boolean isPending() {
            return this == PENDING || this == UNDER_REVIEW;
        }

        /**
         * Get next possible statuses from current status
         */
        public VerificationStatus[] getNextPossibleStatuses() {
            return switch (this) {
                case NOT_SUBMITTED -> new VerificationStatus[]{PENDING};
                case PENDING -> new VerificationStatus[]{UNDER_REVIEW, REJECTED};
                case UNDER_REVIEW -> new VerificationStatus[]{VERIFIED, REJECTED};
                case VERIFIED -> new VerificationStatus[]{EXPIRED};
                case REJECTED -> new VerificationStatus[]{PENDING};
                case EXPIRED -> new VerificationStatus[]{PENDING};
            };
        }
    }

    public enum AvailabilityPattern {
        FULL_TIME("Full-time (40+ hours/week)"),
        PART_TIME("Part-time (20-39 hours/week)"),
        EVENINGS_WEEKENDS("Evenings & Weekends"),
        MORNINGS_ONLY("Mornings Only"),
        FLEXIBLE("Flexible Schedule"),
        BY_APPOINTMENT("By Appointment Only");

        private final String description;

        AvailabilityPattern(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ContactMethod {
        PLATFORM_MESSAGE("Platform Messages"),
        EMAIL("Email"),
        PHONE("Phone Call"),
        TEXT_MESSAGE("Text Message"),
        VIDEO_CALL("Video Call"),
        IN_PERSON("In-Person Consultation");

        private final String description;

        ContactMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum ServiceAreaSelectionMethod {
        CITY_BASED("City/Town Based"),
        DISTANCE_BASED("Distance Radius"),
        MANUAL_ZIPCODE("Manual Zipcode Entry");

        private final String description;

        ServiceAreaSelectionMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
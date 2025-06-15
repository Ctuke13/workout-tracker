package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "professional_profiles")
public class ProfessionalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 RELATIONSHIP TO USER
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 🏢 CORE PROFESSIONAL INFORMATION
    @Column(name = "business_name", length = 100)
    private String businessName; // Optional - for actual business entities

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType = ServiceType.PERSONAL_TRAINER;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_specializations", joinColumns = @JoinColumn(name = "professional_id"))
    @Column(name = "specialization")
    private List<String> specializations;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_certifications", joinColumns = @JoinColumn(name = "professional_id"))
    @Column(name = "certification")
    private List<String> certifications;

    // 💼 SERVICE DETAILS
    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 50, message = "Years of experience seems too high")
    @Column(name = "years_experience")
    private Integer yearsExperience = 0;

    @DecimalMin(value = "0.0", message = "Hourly rate cannot be negative")
    @DecimalMax(value = "1000.0", message = "Hourly rate seems too high")
    @Column(name = "hourly_rate", precision = 8, scale = 2)
    private Double hourlyRate;

    @Pattern(regexp = "^\\d{5}$", message = "Base zipcode must be 5 digits")
    @Column(name = "base_zipcode")
    private String baseZipcode; // Their primary location for distance calculations

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_service_areas", joinColumns = @JoinColumn(name = "professional_id"))
    @Column(name = "zipcode")
    private List<String> serviceAreas; // Zipcodes they serve

    @Enumerated(EnumType.STRING)
    @Column(name = "service_area_selection_method")
    private ServiceAreaSelectionMethod selectionMethod = ServiceAreaSelectionMethod.CITY_BASED;

    @Min(value = 0, message = "Travel distance cannot be negative")
    @Max(value = 500, message = "Travel distance seems unrealistic")
    @Column(name = "max_travel_miles")
    private Integer maxTravelMiles = 15; // How far they'll travel for clients

    @Column(name = "offers_virtual_sessions")
    private Boolean offersVirtualSessions = false;

    @Column(name = "offers_in_home_sessions")
    private Boolean offersInHomeService = false;

    @Column(name = "offers_gym_sessions")
    private Boolean offersGymSessions = true;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "license_number")
    private String licenseNumber; // For licensed professionals (PT, RD, etc.)

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "professional_social_links", joinColumns = @JoinColumn(name = "professional_id"))
    private List<String> socialMediaLinks;

    // 🕐 AVAILABILITY & SCHEDULING
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_pattern")
    private AvailabilityPattern availabilityPattern = AvailabilityPattern.FLEXIBLE;

    @Column(name = "typical_availability", length = 500)
    private String typicalAvailability; // "Mon-Fri 6AM-8PM, Weekends 8AM-6PM"

    @Min(value = 0, message = "Booking lead time cannot be negative")
    @Column(name = "booking_lead_time_hours")
    private Integer bookingLeadTimeHours = 24; // Minimum notice needed

    @Min(value = 15, message = "Session duration must be at least 15 minutes")
    @Max(value = 480, message = "Session duration cannot exceed 8 hours")
    @Column(name = "session_duration_minutes")
    private Integer sessionDurationMinutes = 60; // Default session length

    // 👥 CLIENT MANAGEMENT
    @Column(name = "accepts_new_clients")
    private Boolean acceptsNewClients = true;

    @Column(name = "max_clients")
    private Integer maxClients = 20; // Maximum number of active clients

    @Min(value = 13, message = "Minimum client age too low")
    @Max(value = 120, message = "Maximum client age unrealistic")
    @Column(name = "min_client_age")
    private Integer minClientAge = 16;

    @Min(value = 16, message = "Maximum client age too low")
    @Max(value = 120, message = "Maximum client age unrealistic")
    @Column(name = "max_client_age")
    private Integer maxClientAge = 80;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method")
    private ContactMethod preferredContactMethod = ContactMethod.PLATFORM_MESSAGE;

    @Min(value = 0, message = "Response time cannot be negative")
    @Max(value = 168, message = "Response time cannot exceed 1 week")
    @Column(name = "response_time_hours")
    private Integer responseTimeHours = 4; // How quickly they respond

    // ✅ VERIFICATION & TRUST
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "verification_submitted_at")
    private LocalDateTime verificationSubmittedAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_notes")
    private String verificationNotes; // Admin notes about verification

    @Column(name = "has_liability_insurance")
    private Boolean hasLiabilityInsurance = false;

    @Column(name = "insurance_expiry_date")
    private LocalDateTime insuranceExpiryDate;

    @Column(name = "background_check_completed")
    private Boolean backgroundCheckCompleted = false;

    @Column(name = "background_check_date")
    private LocalDateTime backgroundCheckDate;

    // 📊 PERFORMANCE METRICS
    @Column(name = "total_clients_served")
    private Integer totalClientsServed = 0;

    @Column(name = "active_clients_count")
    private Integer activeClientsCount = 0;

    @Column(name = "total_sessions_completed")
    private Integer totalSessionsCompleted = 0;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    @Column(name = "average_rating", precision = 3, scale = 2)
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "profile_views")
    private Integer profileViews = 0;

    // 💰 BUSINESS SETTINGS
    @Column(name = "accepts_package_deals")
    private Boolean acceptsPackageDeals = false;

    @Column(name = "offers_group_sessions")
    private Boolean offersGroupSessions = false;

    @Column(name = "cancellation_policy", length = 1000)
    private String cancellationPolicy;

    @Column(name = "payment_terms", length = 500)
    private String paymentTerms;

    // 🎯 PROFILE SETTINGS
    @Column(name = "profile_completion_percentage")
    private Integer profileCompletionPercentage = 0; // Calculated field

    @Column(name = "is_profile_public")
    private Boolean isProfilePublic = true;

    @Column(name = "featured_until")
    private LocalDateTime featuredUntil; // Premium placement expiry

    @Column(name = "subscription_tier_required")
    private String subscriptionTierRequired = "PLUS"; // Minimum tier to be professional

    // 🕐 PROFESSIONAL ACTIVITY METHODS
    public String getProfessionalActivityStatus() {
        return user != null ? user.getActivityStatus() : "Unknown";
    }

    public String getProfessionalActivityStatusForViewer(User viewer) {
        return user != null ? user.getActivityStatusForViewer(viewer) : "Unknown";
    }

    public boolean isProfessionalCurrentlyActive() {
        return user != null && user.isCurrentlyActive();
    }

    public boolean isProfessionalActiveToday() {
        return user != null && user.isActiveToday();
    }

    public User.ActivityLevel getProfessionalActivityLevel() {
        return user != null ? user.getActivityLevel() : User.ActivityLevel.INACTIVE;
    }

    // 📊 PROFESSIONAL RESPONSE ANALYTICS
    public String getExpectedResponseTime() {
        if (user == null || !user.isCurrentlyActive()) {
            return responseTimeHours + " hours (typical)";
        }

        User.ActivityLevel level = user.getActivityLevel();
        return switch (level) {
            case ONLINE -> "Active now - quick response expected";
            case RECENTLY_ACTIVE -> "Within " + Math.min(responseTimeHours, 2) + " hours";
            case TODAY -> "Within " + responseTimeHours + " hours";
            case THIS_WEEK -> "1-2 days";
            case INACTIVE -> responseTimeHours + " hours or more";
        };
    }

    public boolean isQuickResponder() {
        return isProfessionalCurrentlyActive() ||
                (isProfessionalActiveToday() && responseTimeHours <= 4);
    }

    // 🎯 PROFESSIONAL AVAILABILITY STATUS
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

    public int getActivityBoostScore() {
        if (user == null) return 0;

        User.ActivityLevel level = user.getActivityLevel();
        return switch (level) {
            case ONLINE -> 100;
            case RECENTLY_ACTIVE -> 80;
            case TODAY -> 60;
            case THIS_WEEK -> 40;
            case INACTIVE -> 20;
        };
    }


    // 📅 TIMESTAMPS
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 📱 ENUMS
    public enum ServiceType {
        PERSONAL_TRAINER("Personal Trainer"),
        FITNESS_COACH("Fitness Coach"),
        YOGA_INSTRUCTOR("Yoga Instructor"),
        PILATES_INSTRUCTOR("Pilates Instructor"),
        NUTRITIONIST("Nutritionist"),
        DIETITIAN("Dietitian"),
        SPORTS_COACH("Sports Coach"),
        PHYSICAL_THERAPIST("Physical Therapist"),
        MASSAGE_THERAPIST("Massage Therapist"),
        GYM_OWNER("Gym Owner"),
        FITNESS_STUDIO_OWNER("Fitness Studio Owner"),
        WELLNESS_COACH("Wellness Coach"),
        STRENGTH_COACH("Strength & Conditioning Coach"),
        REHABILITATION_SPECIALIST("Rehabilitation Specialist"),
        GROUP_FITNESS_INSTRUCTOR("Group Fitness Instructor"),
        ONLINE_COACH("Online Fitness Coach");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
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
        CITY_BASED,      // User selected cities/towns
        DISTANCE_BASED,  // User selected radius from base location
        MANUAL_ZIPCODE   // User manually entered zipcodes
    }

    // 🛠️ UTILITY METHODS
    public boolean isAcceptingClients() {
        return acceptsNewClients &&
                (maxClients == null || activeClientsCount < maxClients) &&
                isProfessionalActiveToday(); // Only active professionals accept clients
    }

    public boolean isActivelyAcceptingClients() {
        return isAcceptingClients() &&
                (isProfessionalCurrentlyActive() || isProfessionalActiveToday());
    }

    public boolean isFullyVerified() {
        return isVerified && hasLiabilityInsurance && backgroundCheckCompleted;
    }

    public boolean canServeLocation(String zipcode) {
        return offersVirtualSessions ||
                (serviceAreas != null && serviceAreas.contains(zipcode));
    }

    public String getVerificationStatus() {
        if (isFullyVerified()) return "FULLY_VERIFIED";
        if (isVerified) return "PARTIALLY_VERIFIED";
        if (verificationSubmittedAt != null) return "PENDING_VERIFICATION";
        return "UNVERIFIED";
    }

    public boolean requiresSubscriptionUpgrade(String userSubscriptionTier) {
        if (subscriptionTierRequired == null) return false;

        return switch (userSubscriptionTier) {
            case "FREE" -> !subscriptionTierRequired.equals("FREE");
            case "PLUS" -> subscriptionTierRequired.equals("PRO");
            case "PRO" -> false;
            default -> true;
        };
    }

    // 🔄 JPA LIFECYCLE METHODS
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (user != null) {
            user.updateLastActive(); // Sync with User's lastActive
        }
        calculateProfileCompletion();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (user != null) {
            user.updateLastActive(); // Sync with User's lastActive
        }
        calculateProfileCompletion();
    }

    // 📊 BUSINESS LOGIC METHODS
    private void calculateProfileCompletion() {
        int completedFields = 0;
        int totalFields = (selectionMethod == ServiceAreaSelectionMethod.DISTANCE_BASED) ? 15 : 14;

        // Business name is optional, so don't count it in completion
        if (bio != null && !bio.trim().isEmpty()) completedFields++;
        if (specializations != null && !specializations.isEmpty()) completedFields++;
        if (certifications != null && !certifications.isEmpty()) completedFields++;
        if (yearsExperience != null && yearsExperience > 0) completedFields++;
        if (hourlyRate != null && hourlyRate > 0) completedFields++;
        if (serviceAreas != null && !serviceAreas.isEmpty()) completedFields++;
        if (selectionMethod == ServiceAreaSelectionMethod.DISTANCE_BASED &&
                baseZipcode != null && !baseZipcode.trim().isEmpty()) {
            completedFields++;
        }
        if (typicalAvailability != null && !typicalAvailability.trim().isEmpty()) completedFields++;
        if (sessionDurationMinutes != null) completedFields++;
        if (minClientAge != null) completedFields++;
        if (maxClientAge != null) completedFields++;
        if (cancellationPolicy != null && !cancellationPolicy.trim().isEmpty()) completedFields++;
        if (paymentTerms != null && !paymentTerms.trim().isEmpty()) completedFields++;
        if (hasLiabilityInsurance != null && hasLiabilityInsurance) completedFields++;
        if (backgroundCheckCompleted != null && backgroundCheckCompleted) completedFields++;

        this.profileCompletionPercentage = (completedFields * 100) / totalFields;
    }

    public void incrementProfileViews() {
        this.profileViews = (this.profileViews == null ? 0 : this.profileViews) + 1;
    }

    public void updateLastActive() {
        if (user != null) {
            user.updateLastActive(); // Delegate to User entity
        }
    }

    public void addCompletedSession() {
        this.totalSessionsCompleted = (this.totalSessionsCompleted == null ? 0 : this.totalSessionsCompleted) + 1;
    }

    public void updateRating(double newRating) {
        if (newRating < 0.0 || newRating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }

        if (this.totalReviews == null) this.totalReviews = 0;
        if (this.averageRating == null) this.averageRating = 0.0;

        double totalRatingPoints = this.averageRating * this.totalReviews;
        this.totalReviews++;
        this.averageRating = (totalRatingPoints + newRating) / this.totalReviews;
    }

    // 🏷️ DISPLAY NAME LOGIC
    public String getDisplayName() {
        // Use business name if provided, otherwise fall back to user's full name
        if (businessName != null && !businessName.trim().isEmpty()) {
            return businessName;
        }

        if (user != null) {
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            return (firstName + " " + lastName).trim();
        }

        return "Professional Profile";
    }

    public String getDisplayNameWithServiceType() {
        String name = getDisplayName();
        String serviceTypeName = serviceType != null ? serviceType.getDisplayName() : "Fitness Professional";
        return name + " - " + serviceTypeName;
    }
}
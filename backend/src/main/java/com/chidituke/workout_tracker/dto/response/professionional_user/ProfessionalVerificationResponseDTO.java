package com.chidituke.workout_tracker.dto.response.professionional_user;

import com.chidituke.workout_tracker.model.ProfessionalProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Response DTO for Professional Verification operations
 * Used when submitting or processing verification requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalVerificationResponseDTO {

    // ==============================================
    // VERIFICATION IDENTIFICATION
    // ==============================================

    private Long verificationId; // ✅ This is the field the service is looking for
    private Long profileId;
    private Long userId;

    // ==============================================
    // VERIFICATION STATUS
    // ==============================================

    private ProfessionalProfile.VerificationStatus status;
    private Boolean isValid; // ✅ This is the field the service is looking for

    // ==============================================
    // VERIFICATION TIMELINE
    // ==============================================

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime expiryDate;

    // ==============================================
    // VERIFICATION DETAILS
    // ==============================================

    private String certificationType;
    private String certificationNumber;
    private String issuingOrganization;
    private String documentationUrl;
    private String reviewNotes;
    private Long reviewedBy; // Admin user ID who processed the verification

    // ==============================================
    // HELPER METHODS
    // ==============================================

    /**
     * Get verification status display name
     */
    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    /**
     * Check if verification is currently valid
     */
    public boolean isCurrentlyValid() {
        return Boolean.TRUE.equals(isValid) &&
                status == ProfessionalProfile.VerificationStatus.VERIFIED &&
                !isExpired();
    }

    /**
     * Check if verification has expired
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false; // No expiry date means it doesn't expire
        }
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Check if verification is expiring soon (within 30 days)
     */
    public boolean isExpiringSoon() {
        if (expiryDate == null) {
            return false;
        }
        LocalDateTime thirtyDaysFromNow = LocalDateTime.now().plusDays(30);
        return expiryDate.isBefore(thirtyDaysFromNow) && !isExpired();
    }

    /**
     * Get days until expiry
     */
    public Long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (expiryDate.isBefore(now)) {
            return -ChronoUnit.DAYS.between(expiryDate, now); // Negative for expired
        }
        return ChronoUnit.DAYS.between(now, expiryDate);
    }

    /**
     * Get verification timeline display
     */
    public String getTimelineDisplay() {
        if (submittedAt == null) {
            return "Not submitted";
        }

        StringBuilder timeline = new StringBuilder();
        timeline.append("Submitted: ").append(submittedAt.toLocalDate());

        if (reviewedAt != null) {
            timeline.append(", Reviewed: ").append(reviewedAt.toLocalDate());
        }

        if (expiryDate != null) {
            timeline.append(", Expires: ").append(expiryDate.toLocalDate());
        }

        return timeline.toString();
    }

    /**
     * Get verification badge for display
     */
    public String getVerificationBadge() {
        if (isCurrentlyValid()) {
            return "✅ Verified";
        }

        if (status != null) {
            switch (status) {
                case UNDER_REVIEW:
                    return "⏳ Pending";
                case REJECTED:
                    return "❌ Rejected";
                case EXPIRED:
                    return "⚠️ Expired";
                case PENDING:
                    return "🔄 Not Submitted";
                default:
                    return "❓ Unknown";
            }
        }

        return "❓ Unknown";
    }

    /**
     * Check if verification needs action
     */
    public boolean needsAction() {
        return status == ProfessionalProfile.VerificationStatus.REJECTED ||
                status == ProfessionalProfile.VerificationStatus.EXPIRED ||
                isExpiringSoon();
    }

    /**
     * Get next action required
     */
    public String getNextAction() {
        if (status == null) {
            return "Submit verification";
        }

        switch (status) {
            case PENDING:
                return "Submit verification documents";
            case UNDER_REVIEW:
                return "Wait for admin review";
            case REJECTED:
                return "Resubmit with corrections";
            case EXPIRED:
                return "Renew verification";
            case VERIFIED:
                if (isExpiringSoon()) {
                    return "Prepare renewal documents";
                } else {
                    return "No action required";
                }
            default:
                return "Contact support";
        }
    }

    /**
     * Get processing time display
     */
    public String getProcessingTimeDisplay() {
        if (submittedAt == null) {
            return "Not applicable";
        }

        if (reviewedAt == null) {
            long daysWaiting = ChronoUnit.DAYS.between(submittedAt, LocalDateTime.now());
            return String.format("%d days waiting", daysWaiting);
        }

        long processingDays = ChronoUnit.DAYS.between(submittedAt, reviewedAt);
        return String.format("Processed in %d days", processingDays);
    }

    /**
     * Create a simplified verification response
     */
    public static ProfessionalVerificationResponseDTO createSimple(Long verificationId,
                                                                   ProfessionalProfile.VerificationStatus status,
                                                                   Boolean isValid) {
        return ProfessionalVerificationResponseDTO.builder()
                .verificationId(verificationId)
                .status(status)
                .isValid(isValid)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create verification response for pending status
     */
    public static ProfessionalVerificationResponseDTO createPending(Long verificationId, Long profileId, Long userId) {
        return ProfessionalVerificationResponseDTO.builder()
                .verificationId(verificationId)
                .profileId(profileId)
                .userId(userId)
                .status(ProfessionalProfile.VerificationStatus.PENDING)
                .isValid(false)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create verification response for approved status
     */
    public static ProfessionalVerificationResponseDTO createApproved(Long verificationId,
                                                                     Long profileId,
                                                                     Long userId,
                                                                     Long reviewedBy) {
        return ProfessionalVerificationResponseDTO.builder()
                .verificationId(verificationId)
                .profileId(profileId)
                .userId(userId)
                .status(ProfessionalProfile.VerificationStatus.VERIFIED)
                .isValid(true)
                .submittedAt(LocalDateTime.now())
                .reviewedAt(LocalDateTime.now())
                .reviewedBy(reviewedBy)
                .build();
    }
}
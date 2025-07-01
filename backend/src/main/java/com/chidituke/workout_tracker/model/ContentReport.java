package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "content_reports", indexes = {
        @Index(name = "idx_content_reports_reporter", columnList = "reporter_id"),
        @Index(name = "idx_content_reports_post", columnList = "reported_post_id"),
        @Index(name = "idx_content_reports_comment", columnList = "reported_comment_id"),
        @Index(name = "idx_content_reports_status", columnList = "status"),
        @Index(name = "idx_content_reports_type_status", columnList = "report_type, status")
})
public class ContentReport {

    public enum ReportType {
        SPAM,
        HARASSMENT,
        HATE_SPEECH,
        VIOLENCE,
        INAPPROPRIATE_CONTENT,
        COPYRIGHT_VIOLATION,
        MISINFORMATION,
        OTHER
    }

    public enum ReportStatus {
        OPEN,
        UNDER_REVIEW,
        RESOLVED_VALID,
        RESOLVED_INVALID,
        DISMISSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @NotNull
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_post_id")
    private SocialPost reportedPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_comment_id")
    private SocialComment reportedComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    @NotNull
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull
    private ReportStatus status = ReportStatus.OPEN;

    @Column(name = "description", length = 1000)
    @Size(max = 1000)
    private String description;

    @Column(name = "moderator_notes", length = 1000)
    @Size(max = 1000)
    private String moderatorNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public ContentReport() {}

    public ContentReport(User reporter, SocialPost reportedPost, ReportType reportType, String description) {
        this.reporter = reporter;
        this.reportedPost = reportedPost;
        this.reportType = reportType;
        this.description = description;
        this.status = ReportStatus.OPEN;
    }

    public ContentReport(User reporter, SocialComment reportedComment, ReportType reportType, String description) {
        this.reporter = reporter;
        this.reportedComment = reportedComment;
        this.reportType = reportType;
        this.description = description;
        this.status = ReportStatus.OPEN;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public SocialPost getReportedPost() {
        return reportedPost;
    }

    public void setReportedPost(SocialPost reportedPost) {
        this.reportedPost = reportedPost;
    }

    public SocialComment getReportedComment() {
        return reportedComment;
    }

    public void setReportedComment(SocialComment reportedComment) {
        this.reportedComment = reportedComment;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModeratorNotes() {
        return moderatorNotes;
    }

    public void setModeratorNotes(String moderatorNotes) {
        this.moderatorNotes = moderatorNotes;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business methods
    public boolean isOpen() {
        return status == ReportStatus.OPEN;
    }

    public boolean isUnderReview() {
        return status == ReportStatus.UNDER_REVIEW;
    }

    public boolean isResolved() {
        return status == ReportStatus.RESOLVED_VALID || status == ReportStatus.RESOLVED_INVALID;
    }

    public boolean isDismissed() {
        return status == ReportStatus.DISMISSED;
    }

    public void markAsUnderReview(User moderator) {
        this.status = ReportStatus.UNDER_REVIEW;
        this.reviewedBy = moderator;
        this.reviewedAt = LocalDateTime.now();
    }

    public void markAsResolvedValid(User moderator, String notes) {
        this.status = ReportStatus.RESOLVED_VALID;
        this.reviewedBy = moderator;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorNotes = notes;
    }

    public void markAsResolvedInvalid(User moderator, String notes) {
        this.status = ReportStatus.RESOLVED_INVALID;
        this.reviewedBy = moderator;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorNotes = notes;
    }

    public void dismiss(User moderator, String notes) {
        this.status = ReportStatus.DISMISSED;
        this.reviewedBy = moderator;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorNotes = notes;
    }

    public String getReportedContentType() {
        if (reportedPost != null) return "POST";
        if (reportedComment != null) return "COMMENT";
        return "UNKNOWN";
    }

    public Long getReportedContentId() {
        if (reportedPost != null) return reportedPost.getId();
        if (reportedComment != null) return reportedComment.getId();
        return null;
    }

    // Validation methods
    public boolean hasValidReportedContent() {
        return (reportedPost != null && reportedComment == null) ||
                (reportedPost == null && reportedComment != null);
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentReport that = (ContentReport) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ContentReport{" +
                "id=" + id +
                ", reporterId=" + (reporter != null ? reporter.getId() : null) +
                ", reportedContentType='" + getReportedContentType() + '\'' +
                ", reportedContentId=" + getReportedContentId() +
                ", reportType=" + reportType +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "post_hashtags", indexes = {
        @Index(name = "idx_post_hashtags_post", columnList = "post_id"),
        @Index(name = "idx_post_hashtags_hashtag", columnList = "hashtag"),
        @Index(name = "idx_post_hashtags_hashtag_created", columnList = "hashtag, created_at")
})
public class PostHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_hashtag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @NotNull
    private SocialPost post;

    @Column(name = "hashtag", nullable = false, length = 100)
    @NotBlank
    @Size(min = 1, max = 100)
    private String hashtag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public PostHashtag() {}

    public PostHashtag(SocialPost post, String hashtag) {
        this.post = post;
        this.hashtag = normalizeHashtag(hashtag);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SocialPost getPost() {
        return post;
    }

    public void setPost(SocialPost post) {
        this.post = post;
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = normalizeHashtag(hashtag);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Business methods
    private String normalizeHashtag(String hashtag) {
        if (hashtag == null) return null;

        // Remove # if present and convert to lowercase
        String normalized = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        return normalized.toLowerCase().trim();
    }

    public String getDisplayHashtag() {
        return hashtag != null ? "#" + hashtag : null;
    }

    public boolean isWorkoutRelated() {
        if (hashtag == null) return false;

        String lower = hashtag.toLowerCase();
        return lower.contains("workout") ||
                lower.contains("fitness") ||
                lower.contains("gym") ||
                lower.contains("exercise") ||
                lower.contains("training") ||
                lower.contains("muscle") ||
                lower.contains("cardio") ||
                lower.contains("strength");
    }

    // Static helper methods
    public static String normalizeHashtagStatic(String hashtag) {
        if (hashtag == null || hashtag.trim().isEmpty()) return null;

        String normalized = hashtag.startsWith("#") ? hashtag.substring(1) : hashtag;
        normalized = normalized.toLowerCase().trim();

        // Remove spaces and special characters except underscore
        normalized = normalized.replaceAll("[^a-z0-9_]", "");

        return normalized.isEmpty() ? null : normalized;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostHashtag that = (PostHashtag) o;
        return Objects.equals(post, that.post) &&
                Objects.equals(hashtag, that.hashtag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(post, hashtag);
    }

    @Override
    public String toString() {
        return "PostHashtag{" +
                "id=" + id +
                ", postId=" + (post != null ? post.getId() : null) +
                ", hashtag='" + hashtag + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
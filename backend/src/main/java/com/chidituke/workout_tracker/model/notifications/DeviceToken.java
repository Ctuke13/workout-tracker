package com.chidituke.workout_tracker.model.notifications;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Stores FCM device tokens per user.
 * Supports WEB, ANDROID, and IOS platforms for React Native migration.
 * <p>
 * Tokens are deactivated (not deleted) on logout to preserve audit trail.
 */
@Entity
@Table(
        name = "device_tokens",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_device_token",
                columnNames = {"user_id", "token"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, columnDefinition = "TEXT")
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 10)
    @Builder.Default
    private Platform platform = Platform.WEB;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── Platform enum ──────────────────────────────────────────────────────────

    public enum Platform {
        WEB,
        ANDROID,
        IOS
    }
}
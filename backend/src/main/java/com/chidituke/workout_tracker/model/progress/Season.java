package com.chidituke.workout_tracker.model.progress;

import com.chidituke.workout_tracker.model.progress.enums.SeasonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "seasons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Season {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_id")
    private Integer seasonId;

    @Column(name = "season_name", nullable = false, length = 50)
    private String seasonName;

    @Enumerated(EnumType.STRING)
    @Column(name = "season_type", nullable = false, length = 20)
    private SeasonType seasonType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Transient
    public boolean isCurrentlyRunning() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    @Transient
    public long getDurationInDays() {
        return startDate.until(endDate).getDays() + 1;
    }
}
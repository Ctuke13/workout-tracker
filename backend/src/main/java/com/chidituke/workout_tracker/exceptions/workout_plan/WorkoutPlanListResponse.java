package com.chidituke.workout_tracker.dto.response.workout_plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for paginated workout plan listings
 * Used for browse/discover workout plans functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutPlanListResponse {

    private List<WorkoutPlanResponse> workoutPlans;

    // Pagination info
    private Integer totalCount;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;

    // Filter info
    private String category;
    private String difficulty;
    private String workoutType;
    private String subscriptionTier;

    // Sort info
    private String sortBy;
    private String sortDirection;

    // Additional metadata
    private Long totalPublicPlans;
    private Long totalUserPlans;
    private Boolean isFiltered;
    private List<String> appliedFilters;

    // Helper methods
    public boolean isEmpty() {
        return workoutPlans == null || workoutPlans.isEmpty();
    }

    public int getResultCount() {
        return workoutPlans != null ? workoutPlans.size() : 0;
    }

    public boolean hasResults() {
        return !isEmpty();
    }

    public boolean isPaginated() {
        return totalPages != null && totalPages > 1;
    }

    public static WorkoutPlanListResponse empty() {
        return WorkoutPlanListResponse.builder()
                .workoutPlans(List.of())
                .totalCount(0)
                .currentPage(0)
                .totalPages(0)
                .hasNext(false)
                .hasPrevious(false)
                .isFiltered(false)
                .build();
    }

    public static WorkoutPlanListResponse single(WorkoutPlanResponse workoutPlan) {
        return WorkoutPlanListResponse.builder()
                .workoutPlans(List.of(workoutPlan))
                .totalCount(1)
                .currentPage(0)
                .totalPages(1)
                .hasNext(false)
                .hasPrevious(false)
                .isFiltered(false)
                .build();
    }
}
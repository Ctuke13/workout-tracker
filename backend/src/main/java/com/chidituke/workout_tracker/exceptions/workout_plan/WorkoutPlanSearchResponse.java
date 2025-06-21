package com.chidituke.workout_tracker.dto.response.workout_plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for workout plan search functionality
 * Includes search metadata and faceted search results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkoutPlanSearchResponse {

    private List<WorkoutPlanResponse> results;

    // Search metadata
    private String searchTerm;
    private Integer totalResults;
    private Integer page;
    private Integer size;
    private Boolean hasMore;
    private LocalDateTime searchTimestamp;

    // Search performance
    private Long searchTimeMs;
    private String searchType; // "FULL_TEXT", "CATEGORY", "FILTERED", etc.

    // Applied filters
    private List<String> appliedFilters;
    private Map<String, Object> activeFilters;

    // Search suggestions and facets
    private List<String> searchSuggestions;
    private SearchFacets facets;

    // Nested class for search facets
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchFacets {
        private Map<String, Long> categoryFacets;
        private Map<String, Long> difficultyFacets;
        private Map<String, Long> workoutTypeFacets;
        private Map<String, Long> durationFacets;
        private Map<String, Long> equipmentFacets;
        private Map<String, Long> muscleGroupFacets;
        private Map<String, Long> subscriptionTierFacets;

        // Helper methods
        public boolean hasFacets() {
            return (categoryFacets != null && !categoryFacets.isEmpty()) ||
                    (difficultyFacets != null && !difficultyFacets.isEmpty()) ||
                    (workoutTypeFacets != null && !workoutTypeFacets.isEmpty());
        }

        public long getTotalFacetCount() {
            long total = 0;
            if (categoryFacets != null) total += categoryFacets.values().stream().mapToLong(Long::longValue).sum();
            if (difficultyFacets != null) total += difficultyFacets.values().stream().mapToLong(Long::longValue).sum();
            if (workoutTypeFacets != null) total += workoutTypeFacets.values().stream().mapToLong(Long::longValue).sum();
            return total;
        }
    }

    // Helper methods
    public boolean hasResults() {
        return results != null && !results.isEmpty();
    }

    public boolean isEmpty() {
        return results == null || results.isEmpty();
    }

    public int getResultCount() {
        return results != null ? results.size() : 0;
    }

    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    public boolean hasFilters() {
        return appliedFilters != null && !appliedFilters.isEmpty();
    }

    public boolean hasSuggestions() {
        return searchSuggestions != null && !searchSuggestions.isEmpty();
    }

    public boolean hasFacets() {
        return facets != null && facets.hasFacets();
    }

    public boolean isTextSearch() {
        return "FULL_TEXT".equals(searchType);
    }

    public boolean isCategorySearch() {
        return "CATEGORY".equals(searchType);
    }

    public boolean isFilteredSearch() {
        return "FILTERED".equals(searchType);
    }

    // Static factory methods
    public static WorkoutPlanSearchResponse empty(String searchTerm) {
        return WorkoutPlanSearchResponse.builder()
                .results(List.of())
                .searchTerm(searchTerm)
                .totalResults(0)
                .page(0)
                .size(0)
                .hasMore(false)
                .searchTimestamp(LocalDateTime.now())
                .appliedFilters(List.of())
                .build();
    }

    public static WorkoutPlanSearchResponse noResults(String searchTerm) {
        return WorkoutPlanSearchResponse.builder()
                .results(List.of())
                .searchTerm(searchTerm)
                .totalResults(0)
                .searchTimestamp(LocalDateTime.now())
                .searchSuggestions(List.of("Try different keywords", "Check spelling", "Use broader terms"))
                .build();
    }

    public static WorkoutPlanSearchResponse fromList(List<WorkoutPlanResponse> results, String searchTerm) {
        return WorkoutPlanSearchResponse.builder()
                .results(results)
                .searchTerm(searchTerm)
                .totalResults(results.size())
                .page(0)
                .size(results.size())
                .hasMore(false)
                .searchTimestamp(LocalDateTime.now())
                .searchType("FULL_TEXT")
                .build();
    }
}
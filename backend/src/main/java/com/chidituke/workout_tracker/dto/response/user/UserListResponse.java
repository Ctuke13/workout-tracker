package com.chidituke.workout_tracker.dto.response.user;

import lombok.Data;
import java.util.List;

@Data
public class UserListResponse {
    private List<UserSearchResponse> users;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    public UserListResponse(List<UserSearchResponse> users, int totalPages, long totalElements,
                            int currentPage, int pageSize, boolean hasNext, boolean hasPrevious) {
        this.users = users;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }
}
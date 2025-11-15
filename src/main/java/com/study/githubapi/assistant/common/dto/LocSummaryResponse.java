package com.study.githubapi.assistant.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocSummaryResponse {
    private String organization;
    private LocalDateTime from;
    private LocalDateTime to;
    private boolean includeForks;
    private boolean includeArchived;
    private LocalDateTime collectedAt;
    private List<UserLocSummary> userSummaries;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLocSummary {
        private String username;
        private String avatarUrl;
        private String htmlUrl;
        private Long totalAdditions;
        private Long totalDeletions;
        private Long totalLoc; // additions + deletions
        private Long totalCommits;
        private List<RepositoryContribution> repositories;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepositoryContribution {
        private String repositoryName;
        private String repositoryFullName;
        private String repositoryUrl;
        private Long additions;
        private Long deletions;
        private Long loc; // additions + deletions
        private Long commits;
        private boolean isFork;
        private boolean isArchived;
        private LocalDateTime lastContribution;
    }
}



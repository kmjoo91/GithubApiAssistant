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
public class LocSummaryOnlyResponse {
    private String organization;
    private LocalDateTime from;
    private LocalDateTime to;
    private boolean includeForks;
    private boolean includeArchived;
    private LocalDateTime collectedAt;
    private List<UserLocSummaryOnly> userSummaries;
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLocSummaryOnly {
        private String username;
        private String avatarUrl;
        private String htmlUrl;
        private Long totalAdditions;
        private Long totalDeletions;
        private Long totalLoc; // additions + deletions
        private Long totalCommits;
        private Integer repositoryCount; // 기여한 레포지토리 수
    }
}



package com.study.githubapi.assistant.user.service;

import com.study.githubapi.assistant.common.LocAggregationHelper;
import com.study.githubapi.assistant.common.dto.LocSummaryResponse;
import com.study.githubapi.github.service.GitHubApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLocService {
    
    private final GitHubApiService gitHubApiService;
    private final LocAggregationHelper locAggregationHelper;
    
    /**
     * 특정 사용자의 모든 레포지토리 LOC 통계를 집계
     */
    public Mono<LocSummaryResponse.UserLocSummary> aggregateUserLocStats(
            String username,
            String token,
            LocalDateTime from,
            LocalDateTime to,
            boolean includeForks,
            boolean includeArchived) {
        
        log.info("Starting LOC aggregation for user: {} from: {} to: {} includeForks: {} includeArchived: {}", 
                username, from, to, includeForks, includeArchived);
        
        return gitHubApiService.getUserRepositories(username, token, includeForks, includeArchived)
                .flatMap(repo -> locAggregationHelper.getRepositoryStats(repo, token, from, to))
                .filter(repoStats -> repoStats != null && !repoStats.getContributorStats().isEmpty())
                .collectList()
                .map(repoStatsList -> {
                    // 사용자별 기여도 맵 생성
                    Map<String, LocAggregationHelper.UserContribution> userContributions = new HashMap<>();
                    
                    for (LocAggregationHelper.RepositoryStats repoStats : repoStatsList) {
                        locAggregationHelper.processRepositoryContributions(repoStats, userContributions);
                    }
                    
                    // 해당 사용자의 기여도만 추출
                    LocAggregationHelper.UserContribution userContribution = userContributions.get(username);
                    
                    if (userContribution == null) {
                        log.warn("No contributions found for user: {}", username);
                        return LocSummaryResponse.UserLocSummary.builder()
                                .username(username)
                                .totalAdditions(0L)
                                .totalDeletions(0L)
                                .totalLoc(0L)
                                .totalCommits(0L)
                                .repositories(Collections.emptyList())
                                .build();
                    }
                    
                    return LocSummaryResponse.UserLocSummary.builder()
                            .username(userContribution.getAuthor().getLogin())
                            .avatarUrl(userContribution.getAuthor().getAvatarUrl())
                            .htmlUrl(userContribution.getAuthor().getHtmlUrl())
                            .totalAdditions(userContribution.getTotalAdditions())
                            .totalDeletions(userContribution.getTotalDeletions())
                            .totalLoc(userContribution.getTotalLoc())
                            .totalCommits(userContribution.getTotalCommits())
                            .repositories(userContribution.getRepositoryContributions())
                            .build();
                })
                .doOnSuccess(response -> log.info("Completed LOC aggregation for user: {}. Total LOC: {}, Total commits: {}", 
                        username, response.getTotalLoc(), response.getTotalCommits()))
                .doOnError(error -> log.error("Failed to aggregate LOC stats for user: {}", username, error));
    }
}


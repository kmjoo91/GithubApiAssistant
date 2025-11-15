package com.study.githubapi.assistant.repository.service;

import com.study.githubapi.assistant.common.LocAggregationHelper;
import com.study.githubapi.assistant.common.dto.LocSummaryOnlyResponse;
import com.study.githubapi.assistant.common.dto.LocSummaryResponse;
import com.study.githubapi.github.service.GitHubApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryLocService {
    
    private final GitHubApiService gitHubApiService;
    private final LocAggregationHelper locAggregationHelper;
    
    /**
     * 조직의 LOC 통계를 집계
     */
    public Mono<LocSummaryResponse> aggregateOrganizationLocStats(
            String organization, 
            String token,
            LocalDateTime from,
            LocalDateTime to,
            boolean includeForks, 
            boolean includeArchived) {
        
        log.info("Starting LOC aggregation for organization: {} from: {} to: {} includeForks: {} includeArchived: {}", 
                organization, from, to, includeForks, includeArchived);
        
        return gitHubApiService.getOrganizationRepositories(organization, token, includeForks, includeArchived)
                .flatMap(repo -> locAggregationHelper.getRepositoryStats(repo, token, from, to))
                .filter(repoStats -> repoStats != null && !repoStats.getContributorStats().isEmpty())
                .collectList()
                .map(repoStatsList -> {
                    LocSummaryResponse response = locAggregationHelper.buildLocSummaryResponse(
                            organization, from, to, includeForks, includeArchived, repoStatsList);
                    log.info("Completed LOC aggregation for organization: {}. Found {} users across {} repositories", 
                            organization, response.getUserSummaries().size(), repoStatsList.size());
                    return response;
                })
                .doOnError(error -> log.error("Failed to aggregate LOC stats for organization: {}", organization, error));
    }
    
    /**
     * 조직의 LOC 통계를 집계 (사용자별 요약만)
     */
    public Mono<LocSummaryOnlyResponse> aggregateOrganizationLocStatsSummary(
            String organization, 
            String token,
            LocalDateTime from,
            LocalDateTime to,
            boolean includeForks, 
            boolean includeArchived) {
        
        log.info("Starting LOC summary aggregation for organization: {} from: {} to: {} includeForks: {} includeArchived: {}", 
                organization, from, to, includeForks, includeArchived);
        
        return gitHubApiService.getOrganizationRepositories(organization, token, includeForks, includeArchived)
                .flatMap(repo -> locAggregationHelper.getRepositoryStats(repo, token, from, to))
                .filter(repoStats -> repoStats != null && !repoStats.getContributorStats().isEmpty())
                .collectList()
                .map(repoStatsList -> locAggregationHelper.buildLocSummaryOnlyResponse(
                        organization, from, to, includeForks, includeArchived, repoStatsList))
                .doOnSuccess(response -> log.info("Completed LOC summary aggregation for organization: {}. Found {} users across {} repositories", 
                        organization, response.getUserSummaries().size(), 
                        response.getMetadata().get("totalRepositories")))
                .doOnError(error -> log.error("Failed to aggregate LOC summary stats for organization: {}", organization, error));
    }
}


package com.study.githubapi.assistant.repository.controller;

import com.study.githubapi.assistant.common.dto.LocSummaryResponse;
import com.study.githubapi.assistant.common.dto.LocSummaryOnlyResponse;
import com.study.githubapi.assistant.repository.service.RepositoryLocService;
import com.study.githubapi.github.service.GitHubApiService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/loc/repository")
@RequiredArgsConstructor
public class RepositoryLocController {
    
    private final RepositoryLocService repositoryLocService;
    private final GitHubApiService gitHubApiService;
    
    /**
     * 조직의 LOC 통계 조회 (레포지토리별 상세 정보 포함)
     * 
     * @param org 조직명
     * @param token GitHub Personal Access Token
     * @param from 집계 시작 시점 (required)
     * @param to 집계 종료 시점 (required)
     * @param includeForks Fork 레포지토리 포함 여부 (default: false)
     * @param includeArchived Archived 레포지토리 포함 여부 (default: false)
     * @return LOC 집계 결과 (레포지토리별 상세 정보 포함)
     */
    @GetMapping("/{org}/detailed")
    public Mono<ResponseEntity<LocSummaryResponse>> getOrganizationLocStatsDetailed(
            @PathVariable @NotBlank String org,
            @RequestParam @NotBlank String token,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "false") boolean includeForks,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        log.info("Received detailed LOC stats request for org: {} from: {} to: {} includeForks: {} includeArchived: {}", 
                org, from, to, includeForks, includeArchived);
        
        return repositoryLocService.aggregateOrganizationLocStats(org, token, from, to, includeForks, includeArchived)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Successfully returned detailed LOC stats for org: {}", org))
                .onErrorResume(error -> {
                    log.error("Failed to get detailed LOC stats for org: {}", org, error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 조직의 LOC 통계 요약 조회 (사용자별 요약만)
     * 
     * @param org 조직명
     * @param token GitHub Personal Access Token
     * @param from 집계 시작 시점 (required)
     * @param to 집계 종료 시점 (required)
     * @param includeForks Fork 레포지토리 포함 여부 (default: false)
     * @param includeArchived Archived 레포지토리 포함 여부 (default: false)
     * @return LOC 집계 요약 결과 (사용자별 요약만)
     */
    @GetMapping("/{org}")
    public Mono<ResponseEntity<LocSummaryOnlyResponse>> getOrganizationLocStatsSummary(
            @PathVariable @NotBlank String org,
            @RequestParam @NotBlank String token,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "false") boolean includeForks,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        log.info("Received summary LOC stats request for org: {} from: {} to: {} includeForks: {} includeArchived: {}", 
                org, from, to, includeForks, includeArchived);
        
        return repositoryLocService.aggregateOrganizationLocStatsSummary(org, token, from, to, includeForks, includeArchived)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Successfully returned summary LOC stats for org: {}", org))
                .onErrorResume(error -> {
                    log.error("Failed to get summary LOC stats for org: {}", org, error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * 조직 내 특정 사용자의 LOC 통계 조회
     * 
     * @param org 조직명
     * @param user 사용자명
     * @param token GitHub Personal Access Token
     * @param from 집계 시작 시점 (required)
     * @param to 집계 종료 시점 (required)
     * @param includeForks Fork 레포지토리 포함 여부 (default: false)
     * @param includeArchived Archived 레포지토리 포함 여부 (default: false)
     * @return 특정 사용자의 LOC 집계 결과
     */
    @GetMapping("/{org}/user/{user}")
    public Mono<ResponseEntity<LocSummaryResponse.UserLocSummary>> getOrganizationUserLocStats(
            @PathVariable @NotBlank String org,
            @PathVariable @NotBlank String user,
            @RequestParam @NotBlank String token,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "false") boolean includeForks,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        log.info("Received user LOC stats request for org: {} user: {} from: {} to: {} includeForks: {} includeArchived: {}", 
                org, user, from, to, includeForks, includeArchived);
        
        return repositoryLocService.aggregateOrganizationLocStats(org, token, from, to, includeForks, includeArchived)
                .map(response -> response.getUserSummaries().stream()
                        .filter(u -> u.getUsername().equals(user))
                        .findFirst()
                        .orElse(null))
                .map(userSummary -> {
                    if (userSummary == null) {
                        log.warn("User {} not found in organization {}", user, org);
                        return ResponseEntity.notFound().<LocSummaryResponse.UserLocSummary>build();
                    }
                    return ResponseEntity.ok(userSummary);
                })
                .doOnSuccess(response -> log.info("Successfully returned user LOC stats for org: {} user: {}", org, user))
                .onErrorResume(error -> {
                    log.error("Failed to get user LOC stats for org: {} user: {}", org, user, error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
    
    /**
     * GitHub API Rate Limit 상태 조회
     * 
     * @param token GitHub Personal Access Token
     * @return Rate Limit 정보
     */
    @GetMapping("/rate-limit")
    public Mono<ResponseEntity<String>> getRateLimitStatus(@RequestParam @NotBlank String token) {
        log.info("Received rate limit status request");
        
        return gitHubApiService.getRateLimitStatus(token)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Successfully returned rate limit status"))
                .onErrorResume(error -> {
                    log.error("Failed to get rate limit status", error);
                    return Mono.just(ResponseEntity.internalServerError()
                            .body("Failed to retrieve rate limit status: " + error.getMessage()));
                });
    }
}


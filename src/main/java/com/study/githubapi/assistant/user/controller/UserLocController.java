package com.study.githubapi.assistant.user.controller;

import com.study.githubapi.assistant.common.dto.LocSummaryResponse;
import com.study.githubapi.assistant.user.service.UserLocService;
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
@RequestMapping("/api/loc/user")
@RequiredArgsConstructor
public class UserLocController {
    
    private final UserLocService userLocService;
    
    /**
     * 특정 사용자의 모든 레포지토리 LOC 통계 조회
     * 
     * @param user 사용자명
     * @param token GitHub Personal Access Token
     * @param from 집계 시작 시점 (required)
     * @param to 집계 종료 시점 (required)
     * @param includeForks Fork 레포지토리 포함 여부 (default: false)
     * @param includeArchived Archived 레포지토리 포함 여부 (default: false)
     * @return 사용자의 LOC 집계 결과
     */
    @GetMapping("/{user}")
    public Mono<ResponseEntity<LocSummaryResponse.UserLocSummary>> getUserLocStats(
            @PathVariable @NotBlank String user,
            @RequestParam @NotBlank String token,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = true) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "false") boolean includeForks,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        log.info("Received user LOC stats request for user: {} from: {} to: {} includeForks: {} includeArchived: {}", 
                user, from, to, includeForks, includeArchived);
        
        return userLocService.aggregateUserLocStats(user, token, from, to, includeForks, includeArchived)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Successfully returned user LOC stats for user: {}", user))
                .onErrorResume(error -> {
                    log.error("Failed to get user LOC stats for user: {}", user, error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}


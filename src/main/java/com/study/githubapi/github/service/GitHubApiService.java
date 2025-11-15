package com.study.githubapi.github.service;

import com.study.githubapi.github.config.GitHubApiConfig;
import com.study.githubapi.github.dto.ContributorStats;
import com.study.githubapi.github.dto.GitHubRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubApiService {
    
    private final WebClient gitHubWebClient;
    private final GitHubApiConfig gitHubApiConfig;
    
    /**
     * 조직의 모든 레포지토리를 페이징 처리로 가져옴
     */
    public Flux<GitHubRepository> getOrganizationRepositories(String org, String token, boolean includeForks, boolean includeArchived) {
        return Flux.range(1, Integer.MAX_VALUE)
                .concatMap(page -> getRepositoriesPage(org, page, includeForks, includeArchived, token))
                .takeWhile(repositories -> !repositories.isEmpty())
                .flatMapIterable(repositories -> repositories)
                .doOnNext(repo -> log.debug("Retrieved repository: {} (fork: {}, archived: {})", 
                    repo.getFullName(), repo.isFork(), repo.isArchived()))
                .doOnComplete(() -> log.info("Completed fetching repositories for organization: {}", org));
    }
    
    /**
     * 사용자의 모든 레포지토리를 페이징 처리로 가져옴
     */
    public Flux<GitHubRepository> getUserRepositories(String username, String token, boolean includeForks, boolean includeArchived) {
        return Flux.range(1, Integer.MAX_VALUE)
                .concatMap(page -> getUserRepositoriesPage(username, page, includeForks, includeArchived, token))
                .takeWhile(repositories -> !repositories.isEmpty())
                .flatMapIterable(repositories -> repositories)
                .doOnNext(repo -> log.debug("Retrieved repository: {} (fork: {}, archived: {})", 
                    repo.getFullName(), repo.isFork(), repo.isArchived()))
                .doOnComplete(() -> log.info("Completed fetching repositories for user: {}", username));
    }
    
    private Mono<List<GitHubRepository>> getUserRepositoriesPage(String username, int page, boolean includeForks, boolean includeArchived, String token) {
        var uri = "/users/{username}/repos?page={page}&per_page=100&sort=updated&direction=desc";
        
        return gitHubWebClient.get()
                .uri(uri, username, page)
                .headers(headers -> applyAuthorizationHeader(headers, token))
                .retrieve()
                .bodyToFlux(GitHubRepository.class)
                .filter(repo -> (includeForks || !repo.isFork()) && (includeArchived || !repo.isArchived()))
                .collectList()
                .retryWhen(Retry.backoff(gitHubApiConfig.getMaxRetry(), Duration.ofMillis(gitHubApiConfig.getRetryDelay()))
                        .filter(this::isRetryableException))
                .doOnError(error -> log.error("Failed to fetch repositories page {} for user {}: {}", 
                    page, username, error.getMessage()))
                .onErrorReturn(Collections.emptyList());
    }
    
    private Mono<List<GitHubRepository>> getRepositoriesPage(String org, int page, boolean includeForks, boolean includeArchived, String token) {
        var uri = "/orgs/{org}/repos?page={page}&per_page=100&sort=updated&direction=desc";
        
        return gitHubWebClient.get()
                .uri(uri, org, page)
                .headers(headers -> applyAuthorizationHeader(headers, token))
                .retrieve()
                .bodyToFlux(GitHubRepository.class)
                .filter(repo -> (includeForks || !repo.isFork()) && (includeArchived || !repo.isArchived()))
                .collectList()
                .retryWhen(Retry.backoff(gitHubApiConfig.getMaxRetry(), Duration.ofMillis(gitHubApiConfig.getRetryDelay()))
                        .filter(this::isRetryableException))
                .doOnError(error -> log.error("Failed to fetch repositories page {} for org {}: {}", 
                    page, org, error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * 레포지토리의 기여자 통계를 가져옴
     * GitHub API는 통계 생성 중일 때 202 Accepted를 반환하므로 재시도 로직 포함
     */
    public Mono<List<ContributorStats>> getRepositoryContributorStats(String owner, String repo, String token) {
        var uri = "/repos/{owner}/{repo}/stats/contributors";
        return gitHubWebClient.get()
                .uri(uri, owner, repo)
                .headers(headers -> applyAuthorizationHeader(headers, token))
                .retrieve()
                .bodyToFlux(ContributorStats.class)
                .collectList()
                .retryWhen(Retry.backoff(gitHubApiConfig.getMaxRetry(), Duration.ofMillis(gitHubApiConfig.getRetryDelay()))
                        .filter(this::isRetryableException))
                .doOnNext(stats -> log.debug("Retrieved contributor stats for {}/{}: {} contributors", 
                    owner, repo, stats.size()))
                .doOnError(error -> log.warn("Failed to fetch contributor stats for {}/{}: {}", 
                    owner, repo, error.getMessage()))
                .onErrorReturn(List.of());
    }
    
    /**
     * GitHub API 호출 중 재시도 가능한 예외인지 확인
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException webClientException) {
            var status = webClientException.getStatusCode();
            // 202 Accepted (통계 생성 중), 403 Forbidden (Rate limit), 500+ 서버 에러는 재시도
            return status == HttpStatus.ACCEPTED || 
                   status == HttpStatus.FORBIDDEN || 
                   status.is5xxServerError();
        }
        return false;
    }
    
    /**
     * API Rate Limit 상태 확인
     */
    public Mono<String> getRateLimitStatus(String token) {
        return gitHubWebClient.get()
                .uri("/rate_limit")
                .headers(headers -> applyAuthorizationHeader(headers, token))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("Rate limit status: {}", response))
                .onErrorReturn("Unable to fetch rate limit status");
    }

    private void applyAuthorizationHeader(HttpHeaders headers, String token) {
        if (StringUtils.hasText(token)) {
            headers.setBearerAuth(token);
        } else {
            headers.remove(HttpHeaders.AUTHORIZATION);
        }
    }
}


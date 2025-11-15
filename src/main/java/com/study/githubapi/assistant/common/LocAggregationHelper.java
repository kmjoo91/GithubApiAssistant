package com.study.githubapi.assistant.common;

import com.study.githubapi.assistant.common.dto.LocSummaryOnlyResponse;
import com.study.githubapi.assistant.common.dto.LocSummaryResponse;
import com.study.githubapi.github.dto.ContributorStats;
import com.study.githubapi.github.dto.GitHubRepository;
import com.study.githubapi.github.service.GitHubApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocAggregationHelper {
    
    private final GitHubApiService gitHubApiService;
    
    /**
     * 레포지토리별 통계 수집
     */
    public Mono<RepositoryStats> getRepositoryStats(GitHubRepository repository, String token, LocalDateTime from, LocalDateTime to) {
        return gitHubApiService.getRepositoryContributorStats(
                repository.getOwner().getLogin(), 
                repository.getName(),
                token)
                .map(contributorStats -> new RepositoryStats(repository, contributorStats, from, to))
                .doOnNext(stats -> log.debug("Collected stats for repository: {} ({} contributors)", 
                    repository.getFullName(), stats.getContributorStats().size()));
    }
    
    /**
     * LocSummaryResponse 생성
     */
    public LocSummaryResponse buildLocSummaryResponse(
            String organization,
            LocalDateTime from,
            LocalDateTime to,
            boolean includeForks,
            boolean includeArchived,
            List<RepositoryStats> repoStatsList) {
        
        Map<String, UserContribution> userContributions = new HashMap<>();
        
        for (RepositoryStats repoStats : repoStatsList) {
            processRepositoryContributions(repoStats, userContributions);
        }
        
        List<LocSummaryResponse.UserLocSummary> userSummaries = userContributions.values().stream()
                .map(this::buildUserLocSummary)
                .sorted(Comparator.comparing(LocSummaryResponse.UserLocSummary::getTotalLoc).reversed())
                .collect(Collectors.toList());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalRepositories", repoStatsList.size());
        metadata.put("totalUsers", userSummaries.size());
        metadata.put("totalLoc", userSummaries.stream().mapToLong(LocSummaryResponse.UserLocSummary::getTotalLoc).sum());
        metadata.put("totalCommits", userSummaries.stream().mapToLong(LocSummaryResponse.UserLocSummary::getTotalCommits).sum());
        
        return LocSummaryResponse.builder()
                .organization(organization)
                .from(from)
                .to(to)
                .includeForks(includeForks)
                .includeArchived(includeArchived)
                .collectedAt(LocalDateTime.now())
                .userSummaries(userSummaries)
                .metadata(metadata)
                .build();
    }
    
    /**
     * LocSummaryOnlyResponse 생성
     */
    public LocSummaryOnlyResponse buildLocSummaryOnlyResponse(
            String organization,
            LocalDateTime from,
            LocalDateTime to,
            boolean includeForks,
            boolean includeArchived,
            List<RepositoryStats> repoStatsList) {
        
        Map<String, UserContribution> userContributions = new HashMap<>();
        
        for (RepositoryStats repoStats : repoStatsList) {
            processRepositoryContributions(repoStats, userContributions);
        }
        
        List<LocSummaryOnlyResponse.UserLocSummaryOnly> userSummaries = userContributions.values().stream()
                .map(this::buildUserLocSummaryOnly)
                .sorted(Comparator.comparing(LocSummaryOnlyResponse.UserLocSummaryOnly::getTotalLoc).reversed())
                .collect(Collectors.toList());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalRepositories", repoStatsList.size());
        metadata.put("totalUsers", userSummaries.size());
        metadata.put("totalLoc", userSummaries.stream().mapToLong(LocSummaryOnlyResponse.UserLocSummaryOnly::getTotalLoc).sum());
        metadata.put("totalCommits", userSummaries.stream().mapToLong(LocSummaryOnlyResponse.UserLocSummaryOnly::getTotalCommits).sum());
        
        return LocSummaryOnlyResponse.builder()
                .organization(organization)
                .from(from)
                .to(to)
                .includeForks(includeForks)
                .includeArchived(includeArchived)
                .collectedAt(LocalDateTime.now())
                .userSummaries(userSummaries)
                .metadata(metadata)
                .build();
    }
    
    /**
     * 레포지토리별 기여도 처리
     */
    public void processRepositoryContributions(
            RepositoryStats repoStats,
            Map<String, UserContribution> userContributions) {
        
        GitHubRepository repository = repoStats.getRepository();
        
        for (ContributorStats contributorStats : repoStats.getContributorStats()) {
            // author가 null인 경우 스킵
            if (contributorStats.getAuthor() == null) {
                log.warn("Skipping contributor stats with null author for repository: {}", repository.getFullName());
                continue;
            }
            
            String username = contributorStats.getAuthor().getLogin();
            
            UserContribution userContribution = userContributions.computeIfAbsent(username,
                    k -> new UserContribution(contributorStats.getAuthor()));
            
            LocSummaryResponse.RepositoryContribution repoContribution =
                    calculateRepositoryContribution(contributorStats, repository, repoStats.getFrom(), repoStats.getTo());
            
            if (repoContribution.getLoc() > 0 || repoContribution.getCommits() > 0) {
                userContribution.addRepositoryContribution(repoContribution);
            }
        }
    }
    
    /**
     * 레포지토리별 기여도 계산
     */
    private LocSummaryResponse.RepositoryContribution calculateRepositoryContribution(
            ContributorStats contributorStats,
            GitHubRepository repository,
            LocalDateTime from,
            LocalDateTime to) {
        
        long fromEpoch = from.toEpochSecond(ZoneOffset.UTC);
        long toEpoch = to.toEpochSecond(ZoneOffset.UTC);
        
        long additions = 0;
        long deletions = 0;
        long commits = 0;
        LocalDateTime lastContribution = null;
        
        if (contributorStats.getWeeks() != null) {
            for (ContributorStats.Week week : contributorStats.getWeeks()) {
                long weekTimestamp = week.getTimestamp();
                
                if (weekTimestamp >= fromEpoch && weekTimestamp <= toEpoch) {
                    additions += week.getAdditions() != null ? week.getAdditions() : 0;
                    deletions += week.getDeletions() != null ? week.getDeletions() : 0;
                    commits += week.getCommits() != null ? week.getCommits() : 0;
                    
                    LocalDateTime weekDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(weekTimestamp), ZoneOffset.UTC);
                    if (lastContribution == null || weekDateTime.isAfter(lastContribution)) {
                        lastContribution = weekDateTime;
                    }
                }
            }
        }
        
        return LocSummaryResponse.RepositoryContribution.builder()
                .repositoryName(repository.getName())
                .repositoryFullName(repository.getFullName())
                .repositoryUrl(repository.getCloneUrl())
                .additions(additions)
                .deletions(deletions)
                .loc(additions + deletions)
                .commits(commits)
                .isFork(repository.isFork())
                .isArchived(repository.isArchived())
                .lastContribution(lastContribution)
                .build();
    }
    
    /**
     * UserLocSummary 생성
     */
    private LocSummaryResponse.UserLocSummary buildUserLocSummary(UserContribution userContribution) {
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
    }
    
    /**
     * UserLocSummaryOnly 생성
     */
    private LocSummaryOnlyResponse.UserLocSummaryOnly buildUserLocSummaryOnly(UserContribution userContribution) {
        return LocSummaryOnlyResponse.UserLocSummaryOnly.builder()
                .username(userContribution.getAuthor().getLogin())
                .avatarUrl(userContribution.getAuthor().getAvatarUrl())
                .htmlUrl(userContribution.getAuthor().getHtmlUrl())
                .totalAdditions(userContribution.getTotalAdditions())
                .totalDeletions(userContribution.getTotalDeletions())
                .totalLoc(userContribution.getTotalLoc())
                .totalCommits(userContribution.getTotalCommits())
                .repositoryCount(userContribution.getRepositoryContributions().size())
                .build();
    }
    
    /**
     * 레포지토리별 통계를 담는 내부 클래스
     */
    public static class RepositoryStats {
        private final GitHubRepository repository;
        private final List<ContributorStats> contributorStats;
        private final LocalDateTime from;
        private final LocalDateTime to;
        
        public RepositoryStats(GitHubRepository repository, List<ContributorStats> contributorStats, 
                             LocalDateTime from, LocalDateTime to) {
            this.repository = repository;
            this.contributorStats = contributorStats;
            this.from = from;
            this.to = to;
        }
        
        public GitHubRepository getRepository() {
            return repository;
        }
        
        public List<ContributorStats> getContributorStats() {
            return contributorStats;
        }
        
        public LocalDateTime getFrom() {
            return from;
        }
        
        public LocalDateTime getTo() {
            return to;
        }
    }
    
    /**
     * 사용자별 기여도를 담는 내부 클래스
     */
    public static class UserContribution {
        private final ContributorStats.Author author;
        private final List<LocSummaryResponse.RepositoryContribution> repositoryContributions;
        private long totalAdditions = 0;
        private long totalDeletions = 0;
        private long totalCommits = 0;
        
        public UserContribution(ContributorStats.Author author) {
            this.author = author;
            this.repositoryContributions = new ArrayList<>();
        }
        
        public void addRepositoryContribution(LocSummaryResponse.RepositoryContribution contribution) {
            repositoryContributions.add(contribution);
            totalAdditions += contribution.getAdditions();
            totalDeletions += contribution.getDeletions();
            totalCommits += contribution.getCommits();
        }
        
        public ContributorStats.Author getAuthor() {
            return author;
        }
        
        public List<LocSummaryResponse.RepositoryContribution> getRepositoryContributions() {
            return repositoryContributions;
        }
        
        public long getTotalAdditions() {
            return totalAdditions;
        }
        
        public long getTotalDeletions() {
            return totalDeletions;
        }
        
        public long getTotalLoc() {
            return totalAdditions + totalDeletions;
        }
        
        public long getTotalCommits() {
            return totalCommits;
        }
    }
}


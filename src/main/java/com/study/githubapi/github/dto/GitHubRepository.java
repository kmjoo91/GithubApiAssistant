package com.study.githubapi.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GitHubRepository {
    private Long id;
    private String name;
    
    @JsonProperty("full_name")
    private String fullName;
    
    private String description;
    private boolean fork;
    private boolean archived;
    private boolean disabled;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("pushed_at")
    private LocalDateTime pushedAt;
    
    @JsonProperty("clone_url")
    private String cloneUrl;
    
    @JsonProperty("ssh_url")
    private String sshUrl;
    
    @JsonProperty("default_branch")
    private String defaultBranch;
    
    private String language;
    
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;
    
    @JsonProperty("watchers_count")
    private Integer watchersCount;
    
    @JsonProperty("forks_count")
    private Integer forksCount;
    
    @JsonProperty("open_issues_count")
    private Integer openIssuesCount;
    
    private Owner owner;
    
    @Data
    public static class Owner {
        private Long id;
        private String login;
        private String type;
        
        @JsonProperty("avatar_url")
        private String avatarUrl;
        
        @JsonProperty("html_url")
        private String htmlUrl;
    }
}


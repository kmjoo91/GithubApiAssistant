package com.study.githubapi.github.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ContributorStats {
    private Long total;
    private Author author;
    private List<Week> weeks;
    
    @Data
    public static class Author {
        private Long id;
        private String login;
        
        @JsonProperty("avatar_url")
        private String avatarUrl;
        
        @JsonProperty("html_url")
        private String htmlUrl;
        
        private String type;
    }
    
    @Data
    public static class Week {
        @JsonProperty("w")
        private Long timestamp; // Unix timestamp
        
        @JsonProperty("a")
        private Integer additions;
        
        @JsonProperty("d")
        private Integer deletions;
        
        @JsonProperty("c")
        private Integer commits;
    }
}


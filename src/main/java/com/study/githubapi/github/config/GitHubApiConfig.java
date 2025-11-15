package com.study.githubapi.github.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "github.api")
@Data
public class GitHubApiConfig {
    private String baseUrl = "https://api.github.com";
    private int timeout = 30000;
    private int maxRetry = 3;
    private int retryDelay = 2000;
    private RateLimit rateLimit = new RateLimit();
    
    @Data
    public static class RateLimit {
        private int requestsPerHour = 5000;
    }
    
    @Bean
    public WebClient gitHubWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("User-Agent", "GitHubApiAssistant/1.0")
                .build();
    }
    
    /**
     * 토큰을 포함한 WebClient 생성
     */
    public WebClient createWebClientWithToken(String token) {
        var builder = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("User-Agent", "GitHubApiAssistant/1.0");
        
        if (token != null && !token.isEmpty()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }
        
        return builder.build();
    }
}


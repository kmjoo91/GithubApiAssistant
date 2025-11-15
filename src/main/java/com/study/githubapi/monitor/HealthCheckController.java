package com.study.githubapi.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/monitor")
public class HealthCheckController {
    
    /**
     * 헬스 체크 엔드포인트
     * 
     * @return 서비스 상태
     */
    @GetMapping("/health-check")
    public Mono<ResponseEntity<String>> healthCheck() {
        log.debug("Health check requested");
        return Mono.just(ResponseEntity.ok("GitHub API Assistant is running"));
    }
}


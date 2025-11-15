# GitHub API Assistant

GitHub Organization의 모든 레포지토리에 대해 각 사용자별 LOC(Line of Code)와 커밋 수를 자동으로 집계하는 Spring Boot 애플리케이션입니다.

> 🚀 **Java 24** + **Kotlin DSL** + **Lombok 1.18.38**로 구축된 최신 기술 스택 프로젝트

## 🎯 프로젝트 목적

실무에 가까운 외부 API 연동, 비동기 처리, 데이터 집계/저장, REST API 제공 흐름을 학습하기 위한 프로젝트입니다.

### 🆕 최신 기술 적용
- **Java 24** 최신 기능 활용
- **Kotlin DSL**을 사용한 타입 안전한 빌드 스크립트
- **Lombok 1.18.38**로 Java 24 호환성 확보

## 🛠️ 기술 스택

- **Java 24** - 최신 LTS 버전
- **Spring Boot 3.4.1** - Java 24 지원
- **Gradle 8.14.3** - Kotlin DSL 사용
- **Spring WebFlux (WebClient)** - 비동기 처리
- **JUnit 5** - 테스트 프레임워크
- **Lombok 1.18.38** - Java 24 호환

## 🚀 주요 기능

### 1. 레포지토리 수집
- API: `GET /orgs/{org}/repos`
- 조직 내 모든 레포지토리 목록을 페이징 처리로 수집
- Fork 레포지토리 제외/포함 옵션
- Archived 레포지토리 제외/포함 옵션

### 2. 기여자 LOC 통계 수집
- API: `GET /repos/{owner}/{repo}/stats/contributors`
- 레포별 기여자 통계를 가져오고, 주간 단위 additions, deletions, commits를 합산
- GitHub의 202 Accepted(통계 생성 중) 응답을 고려한 재시도 로직 구현

### 3. 사용자별 LOC 집계
- 특정 기간(from/to 파라미터)의 데이터만 집계 가능
- 사용자 단위로 전체 레포지토리에서 LOC/커밋 합계를 계산
- 레포지토리 단위별 상세 데이터도 함께 제공
- 조직 내 특정 사용자와 전체 사용자 단일 조회 API(`/api/loc/repository/{org}/user/{user}`, `/api/loc/user/{user}`) 지원

### 4. REST API 제공

#### 4.1 Repository LOC API
- `GET /api/loc/repository/{org}`: 조직의 LOC 통계 요약 (사용자별 합계만 반환)
- `GET /api/loc/repository/{org}/detailed`: 조직의 LOC 통계 상세 (사용자별 + 레포지토리별 세부 내역)
- `GET /api/loc/repository/{org}/user/{user}`: 특정 조직 안 특정 사용자의 기여도 확인
- `GET /api/loc/repository/rate-limit`: GitHub API Rate Limit 상태 확인

모든 Repository LOC API는 다음 공통 쿼리 파라미터를 사용합니다:
- `token` (required): GitHub Personal Access Token
- `from` (required, ISO DateTime): 집계 시작 시점
- `to` (required, ISO DateTime): 집계 종료 시점
- `includeForks` (optional, default: false): Fork 레포지토리 포함 여부
- `includeArchived` (optional, default: false): Archived 레포지토리 포함 여부

#### 4.2 User LOC API
- `GET /api/loc/user/{user}`: 특정 사용자의 모든 퍼블릭 레포지토리에 대한 LOC/커밋 합계를 조회 (조직 구분 없이)

User LOC API 또한 위와 동일한 쿼리 파라미터를 사용합니다.

#### 4.3 모니터링 API
- `GET /monitor/health-check`: 애플리케이션 헬스 체크

## 📋 API 명세

### 1. Repository LOC 통계 요약 (사용자별 요약만)
```http
GET /api/loc/repository/{org}?token=your_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59&includeForks=false&includeArchived=false
```

**응답 예시:**
```json
{
  "organization": "spring-projects",
  "from": "2024-01-01T00:00:00",
  "to": "2024-12-31T23:59:59",
  "includeForks": false,
  "includeArchived": false,
  "collectedAt": "2024-12-24T10:30:00",
  "userSummaries": [
    {
      "username": "developer1",
      "avatarUrl": "https://avatars.githubusercontent.com/u/12345",
      "htmlUrl": "https://github.com/developer1",
      "totalAdditions": 15000,
      "totalDeletions": 3000,
      "totalLoc": 18000,
      "totalCommits": 145,
      "repositoryCount": 8
    },
    {
      "username": "developer2",
      "avatarUrl": "https://avatars.githubusercontent.com/u/67890",
      "htmlUrl": "https://github.com/developer2",
      "totalAdditions": 12000,
      "totalDeletions": 2500,
      "totalLoc": 14500,
      "totalCommits": 98,
      "repositoryCount": 5
    },
    {
      "username": "developer3",
      "avatarUrl": "https://avatars.githubusercontent.com/u/11111",
      "htmlUrl": "https://github.com/developer3",
      "totalAdditions": 8000,
      "totalDeletions": 1200,
      "totalLoc": 9200,
      "totalCommits": 67,
      "repositoryCount": 3
    }
  ],
  "metadata": {
    "totalRepositories": 25,
    "totalUsers": 3,
    "totalLoc": 41700,
    "totalCommits": 310,
    "forkRepositories": 0,
    "archivedRepositories": 2
  }
}
```

### 2. Repository LOC 통계 상세 조회 (레포지토리별 상세 정보 포함)
```http
GET /api/loc/repository/{org}/detailed?token=your_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59&includeForks=false&includeArchived=false
```

**응답 예시:**
```json
{
  "organization": "spring-projects",
  "from": "2024-01-01T00:00:00",
  "to": "2024-12-31T23:59:59",
  "includeForks": false,
  "includeArchived": false,
  "collectedAt": "2024-12-24T10:30:00",
  "userSummaries": [
    {
      "username": "developer1",
      "avatarUrl": "https://avatars.githubusercontent.com/u/12345",
      "htmlUrl": "https://github.com/developer1",
      "totalAdditions": 15000,
      "totalDeletions": 3000,
      "totalLoc": 18000,
      "totalCommits": 145,
      "repositories": [
        {
          "repositoryName": "spring-boot",
          "repositoryFullName": "spring-projects/spring-boot",
          "repositoryUrl": "https://github.com/spring-projects/spring-boot",
          "additions": 8000,
          "deletions": 1500,
          "loc": 9500,
          "commits": 67,
          "isFork": false,
          "isArchived": false,
          "lastContribution": "2024-12-20T14:30:00"
        }
      ]
    }
  ],
  "metadata": {
    "totalRepositories": 25,
    "totalUsers": 48,
    "totalLoc": 250000,
    "totalCommits": 1240,
    "forkRepositories": 0,
    "archivedRepositories": 2
  }
}
```

### 3. 특정 조직 내 사용자 LOC 조회
```http
GET /api/loc/repository/{org}/user/{user}?token=your_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
```

**응답 예시:**
```json
{
  "username": "developer1",
  "avatarUrl": "https://avatars.githubusercontent.com/u/12345",
  "htmlUrl": "https://github.com/developer1",
  "totalAdditions": 15000,
  "totalDeletions": 3000,
  "totalLoc": 18000,
  "totalCommits": 145,
  "repositories": [
    {
      "repositoryName": "spring-boot",
      "repositoryFullName": "spring-projects/spring-boot",
      "repositoryUrl": "https://github.com/spring-projects/spring-boot",
      "additions": 8000,
      "deletions": 1500,
      "loc": 9500,
      "commits": 67,
      "isFork": false,
      "isArchived": false,
      "lastContribution": "2024-12-20T14:30:00"
    }
  ]
}
```

### 4. 특정 사용자의 전체 레포지토리 LOC 조회
```http
GET /api/loc/user/{user}?token=your_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59&includeForks=true
```

**응답 예시:**
```json
{
  "username": "developer1",
  "avatarUrl": "https://avatars.githubusercontent.com/u/12345",
  "htmlUrl": "https://github.com/developer1",
  "totalAdditions": 21000,
  "totalDeletions": 4500,
  "totalLoc": 25500,
  "totalCommits": 210,
  "repositories": [
    {
      "repositoryName": "spring-boot",
      "repositoryFullName": "spring-projects/spring-boot",
      "repositoryUrl": "https://github.com/spring-projects/spring-boot",
      "additions": 8000,
      "deletions": 1500,
      "loc": 9500,
      "commits": 67,
      "isFork": false,
      "isArchived": false,
      "lastContribution": "2024-12-20T14:30:00"
    }
  ]
}
```

### 5. Rate Limit 상태 조회
```http
GET /api/loc/repository/rate-limit?token=your_token
```

### 6. 헬스 체크
```http
GET /monitor/health-check
```

## ⚙️ 설정

### GitHub Personal Access Token 사용

GitHub Personal Access Token은 API 호출 시 파라미터로 전달합니다. 환경변수나 설정 파일에 저장하지 않아도 됩니다.

### 애플리케이션 설정

`src/main/resources/application.yml`에서 다음 설정을 조정할 수 있습니다:

- GitHub API timeout 설정
- 재시도 정책
- Rate Limit 설정
- 로깅 레벨

## 🚀 실행 방법

### 1. 프로젝트 클론 및 빌드
```bash
./gradlew clean build
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 테스트 실행
```bash
./gradlew test
```

### 4. 빌드 스크립트 정보
- **Kotlin DSL** 사용으로 타입 안전성과 IDE 지원 향상
- `build.gradle.kts` 파일로 빌드 설정 관리
- Java 24와 Lombok 1.18.38 완전 호환

## 📊 사용 예시

### 조직의 LOC 통계 요약 조회 (빠른 응답)
```bash
curl "http://localhost:8080/api/loc/repository/spring-projects?token=your_github_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59"
```

### 조직의 LOC 통계 상세 조회 (레포지토리별 정보 포함)
```bash
curl "http://localhost:8080/api/loc/repository/spring-projects/detailed?token=your_github_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59"
```

### 조직 내 특정 사용자 통계 조회
```bash
curl "http://localhost:8080/api/loc/repository/spring-projects/user/developer1?token=your_github_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59"
```

### 특정 사용자의 전체 레포지토리 통계 조회
```bash
curl "http://localhost:8080/api/loc/user/developer1?token=your_github_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59&includeForks=true"
```

### Fork 레포지토리를 포함하여 조직 통계 조회
```bash
curl "http://localhost:8080/api/loc/repository/spring-projects?token=your_github_token&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59&includeForks=true"
```

### Rate Limit 상태 조회
```bash
curl "http://localhost:8080/api/loc/repository/rate-limit?token=your_github_token"
```

### 헬스 체크
```bash
curl "http://localhost:8080/monitor/health-check"
```

## 🔍 주요 특징

1. **비동기 처리**: Spring WebFlux를 사용한 논블로킹 I/O
2. **재시도 로직**: GitHub API의 202 Accepted 응답 처리
3. **페이징 처리**: 대용량 조직의 레포지토리 목록 처리
4. **Rate Limit 고려**: GitHub API Rate Limit 모니터링
5. **에러 핸들링**: 글로벌 예외 처리기로 일관된 에러 응답
6. **설정 외부화**: 환경에 따른 설정 관리
7. **최신 기술 스택**: Java 24, Spring Boot 3.4.1, Kotlin DSL 활용
8. **타입 안전성**: Kotlin DSL로 빌드 스크립트의 타입 안전성 확보
9. **개발 생산성**: Lombok 1.18.38로 보일러플레이트 코드 최소화

## 📝 개발 고려사항

### API 제한사항
- GitHub API Rate Limit (시간당 5,000 요청)
- 대용량 조직의 경우 처리 시간이 오래 걸릴 수 있음
- 통계 데이터가 없는 레포지토리는 제외됨
- Fork 레포지토리의 경우 원본 레포지토리와 중복 계산 가능성

### 기술 스택 고려사항
- **Java 24**: 최신 LTS 버전으로 성능 향상 및 새로운 언어 기능 활용
- **Kotlin DSL**: 빌드 스크립트의 타입 안전성과 IDE 지원 향상
- **Lombok 1.18.38**: Java 24와 완전 호환되는 최신 버전 사용
- **Spring Boot 3.4.1**: Java 24 지원 및 최신 Spring 기능 활용

## 🤝 기여 방법

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 라이센스

This project is licensed under the MIT License.

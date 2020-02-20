package me.kmjoo91.githubapiassistant.common.type;

import org.springframework.http.HttpMethod;

import lombok.Getter;

@Getter
public enum GithubApiType {
	REPOSITORIES("/user/repos", HttpMethod.GET),
	TEAM_REPOSITORIES("/orgs/{org}/teams/{team}/repos", HttpMethod.GET),
	COMMITS("/repos/{owner}/{repository}/commits", HttpMethod.GET),
	SINGLE_COMMIT("/repos/{owner}/{repository}/commits/{sha}", HttpMethod.GET);

	private final String path;
	private final HttpMethod method;

	GithubApiType(String path, HttpMethod method) {
		this.path = path;
		this.method = method;
	}
}

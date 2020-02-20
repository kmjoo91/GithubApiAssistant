package me.kmjoo91.githubapiassistant.common.githubapi;

import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import me.kmjoo91.githubapiassistant.commit.model.CommitDetail;
import me.kmjoo91.githubapiassistant.commit.model.GitCommitsParameter;
import me.kmjoo91.githubapiassistant.common.model.Pagination;
import me.kmjoo91.githubapiassistant.common.model.QueryStringCreator;
import me.kmjoo91.githubapiassistant.common.type.GithubApiType;
import me.kmjoo91.githubapiassistant.repository.model.RepositoriesParameter;
import me.kmjoo91.githubapiassistant.repository.model.Repository;
import me.kmjoo91.githubapiassistant.singlecommit.model.SingleCommit;
import me.kmjoo91.githubapiassistant.singlecommit.model.SingleCommitParameter;

@Component
public class HttpsGithubApi implements GithubApi {
	private final static String HTTPS_SCHEMA = "https";
	private final static String DEFAULT_GITHUB_API_HOST = "api.github.com";
	private static final int SECONDS = 1000;
	private static final List<HttpMethod> SUPPORT_METHOD = Collections.singletonList(HttpMethod.GET);

	@Override
	public List<Repository> getRepositories(String personalToken, RepositoriesParameter repositoriesParameter, Pagination pagination) {
		return getRepositories(personalToken, repositoriesParameter, DEFAULT_GITHUB_API_HOST, pagination);
	}

	@Override
	public List<Repository> getRepositories(String personalToken, RepositoriesParameter repositoriesParameter, String apiHost, Pagination pagination) {
		Repository[] repositoriesApiResponse = callApi(apiHost, personalToken, GithubApiType.REPOSITORIES, null, repositoriesParameter, StandardCharsets.UTF_8, Repository[].class, pagination);

		return Arrays.asList(repositoriesApiResponse);
	}

	@Override
	public List<CommitDetail> getCommits(String personalToken, GitCommitsParameter gitCommitsParameter, Pagination pagination) {
		return getCommits(personalToken, gitCommitsParameter, DEFAULT_GITHUB_API_HOST, pagination);
	}

	@Override
	public List<CommitDetail> getCommits(String personalToken, GitCommitsParameter gitCommitsParameter, String apiHost, Pagination pagination) {
		CommitDetail[] commitApiResponse = callApi(apiHost, personalToken, GithubApiType.COMMITS, gitCommitsParameter, gitCommitsParameter, StandardCharsets.UTF_8, CommitDetail[].class, pagination);

		return Arrays.asList(commitApiResponse);
	}

	@Override
	public SingleCommit getSingleCommit(String personalToken, SingleCommitParameter singleCommitParameter, Pagination pagination) {
		return getSingleCommit(personalToken, singleCommitParameter, DEFAULT_GITHUB_API_HOST, pagination);
	}

	@Override
	public SingleCommit getSingleCommit(String personalToken, SingleCommitParameter singleCommitParameter, String apiHost, Pagination pagination) {
		return callApi(apiHost, personalToken, GithubApiType.SINGLE_COMMIT, singleCommitParameter, null, StandardCharsets.UTF_8, SingleCommit.class, pagination);
	}

	private <T, PV> T callApi(String host, String personalToken, GithubApiType githubApiType, PV expand, QueryStringCreator queryStringCreator, Charset encodeCharset, Class<T> resultType, Pagination pagination) {
		URI uri = createUri(host, githubApiType, expand, queryStringCreator, encodeCharset, pagination);
		RestTemplate restTemplate = createRestTemplate(30 * SECONDS, SECONDS);
		HttpHeaders headers = new HttpHeaders();

		// set `Content-Type` and `Accept` headers
		headers.set("Authorization",String.format("token %s", personalToken));

		HttpEntity httpEntity = new HttpEntity(headers);

		ResponseEntity<T> responseEntity;

		switch (githubApiType.getMethod()) {
			case GET:
				responseEntity = restTemplate.exchange(uri, HttpMethod.GET, httpEntity, resultType);
				break;
			default:
				throw new MethodNotAllowedException(githubApiType.getMethod(), SUPPORT_METHOD);
		}

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			return responseEntity.getBody();
		} else {
			throw new HttpClientErrorException(responseEntity.getStatusCode());
		}
	}

	private <PV> URI createUri(String host, GithubApiType githubApiType, PV expand, QueryStringCreator queryStringCreator, Charset encodeCharset, Pagination pagination) {
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
			.scheme(HttpsGithubApi.HTTPS_SCHEMA)
			.host(host);


		//PathVariable셋팅
		if (StringUtils.isNotBlank(githubApiType.getPath())) {
			uriComponentsBuilder.path(githubApiType.getPath());
		}


		//QueryString 셋팅
		uriComponentsBuilder.queryParams(pagination.createQueryStringMap());

		if (!Objects.isNull(queryStringCreator)) {
			uriComponentsBuilder.queryParams(queryStringCreator.createQueryStringMap());
		}

		UriComponents uriComponents = uriComponentsBuilder.build();


		//PathVariable 치환
		if (StringUtils.isNotBlank(githubApiType.getPath()) && Objects.nonNull(expand)) {
			uriComponents.expand(expand);
		}

		uriComponents.encode(encodeCharset);

		return uriComponents.toUri();
	}

	private RestTemplate createRestTemplate(int readTimeout, int connectionTimeout) {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setReadTimeout(readTimeout);
		factory.setConnectTimeout(connectionTimeout);
		return new RestTemplate(factory);
	}
}

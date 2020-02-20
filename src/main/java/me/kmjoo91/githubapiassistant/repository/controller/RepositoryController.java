package me.kmjoo91.githubapiassistant.repository.controller;

import java.nio.file.InvalidPathException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import me.kmjoo91.githubapiassistant.common.githubapi.GithubApi;
import me.kmjoo91.githubapiassistant.common.model.Pagination;
import me.kmjoo91.githubapiassistant.repository.model.RepositoriesParameter;
import me.kmjoo91.githubapiassistant.repository.model.Repository;

@RestController
@RequestMapping(value = "/repositories")
public class RepositoryController {
	private final GithubApi githubApi;

	public RepositoryController(GithubApi githubApi) {
		this.githubApi = githubApi;
	}

	/**
	 * Github 유저의 Repository 목록을 반환한다.
	 * https://developer.github.com/v3/repos/
	 *
	 * @param personalToken 해당 유저의 Github 권한관리를 위한 토큰
	 * @param repositoriesParameter 위의 api설명에 나와있는 parameter를 받기위한 변수
	 * @return Repository 목록
	 */
	@GetMapping("/{personalToken}/user")
	public List<Repository> getUserRepositories(@PathVariable String personalToken, RepositoriesParameter repositoriesParameter, Pagination pagination) {
		if (StringUtils.isBlank(personalToken)) {
			throw new InvalidPathException(personalToken, "PersonalToken is Blank");
		}

		return githubApi.getRepositories(personalToken, repositoriesParameter, pagination);
	}

	/**
	 * Github 유저의 Repository 목록을 반환한다.
	 * https://developer.github.com/v3/repos/
	 *
	 * @param personalToken 해당 유저의 Github 권한관리를 위한 토큰
	 * @param apiHost Enterprise 버전의 경우 apiUrl이 달라 받기위한 변수
	 * @param repositoriesParameter 위의 api설명에 나와있는 parameter를 받기위한 변수
	 * @return Repository 목록
	 */
	@GetMapping(value = "/{personalToken}/user", params = "apiHost")
	public List<Repository> getEnterpriseUserRepositories(@PathVariable String personalToken, @RequestParam("apiHost") String apiHost, RepositoriesParameter repositoriesParameter, Pagination pagination) {
		if (StringUtils.isBlank(personalToken)) {
			throw new InvalidPathException(personalToken, "PersonalToken is Blank");
		}

		if (StringUtils.isBlank(apiHost)) {
			throw new IllegalArgumentException("ApiUrl is Blank");
		}

		return githubApi.getRepositories(personalToken, repositoriesParameter, apiHost, pagination);
	}
}

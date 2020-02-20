package me.kmjoo91.githubapiassistant.common.githubapi;

import java.util.List;

import me.kmjoo91.githubapiassistant.commit.model.CommitDetail;
import me.kmjoo91.githubapiassistant.commit.model.GitCommitsParameter;
import me.kmjoo91.githubapiassistant.common.model.Pagination;
import me.kmjoo91.githubapiassistant.repository.model.RepositoriesParameter;
import me.kmjoo91.githubapiassistant.repository.model.Repository;
import me.kmjoo91.githubapiassistant.singlecommit.model.SingleCommit;
import me.kmjoo91.githubapiassistant.singlecommit.model.SingleCommitParameter;

public interface GithubApi {
	/**
	 * Github 유저의 Repository 목록을 반환한다.
	 * https://developer.github.com/v3/repos/
	 *
	 * @param personalToken 해당 유저의 Github 권한관리를 위한 토큰
	 * @param repositoriesParameter 위의 api설명에 나와있는 parameter를 받기위한 변수
	 * @return Repository 목록
	 */
	List<Repository> getRepositories(String personalToken, RepositoriesParameter repositoriesParameter, Pagination pagination);
	/**
	 * Github 유저의 Repository 목록을 반환한다.
	 * https://developer.github.com/v3/repos/
	 *
	 * @param personalToken 해당 유저의 Github 권한관리를 위한 토큰
	 * @param repositoriesParameter 위의 api설명에 나와있는 parameter를 받기위한 변수
	 * @param apiHost Enterprise 버전의 경우 apiUrl이 달라 받기위한 변수
	 * @return Repository 목록
	 */
	List<Repository> getRepositories(String personalToken, RepositoriesParameter repositoriesParameter, String apiHost, Pagination pagination);
	/**
	 * GitCommitParameter의 필수 값인 owner, repository 값에 맞는 Commit 목록을 가져오는 함수
	 *
	 * @param personalToken
	 * @param gitCommitsParameter
	 * @return
	 */
	List<CommitDetail> getCommits(String personalToken, GitCommitsParameter gitCommitsParameter, Pagination pagination);
	List<CommitDetail> getCommits(String personalToken, GitCommitsParameter gitCommitsParameter, String apiHost, Pagination pagination);
	SingleCommit getSingleCommit(String personalToken, SingleCommitParameter singleCommitParameter, Pagination pagination);
	SingleCommit getSingleCommit(String personalToken, SingleCommitParameter singleCommitParameter, String apiHost, Pagination pagination);
}

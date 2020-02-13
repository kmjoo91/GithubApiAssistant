package me.kmjoo91.githubapiassistant.repository.model;

import java.util.List;

public class CommitDetail {
	private String sha;
	private String nodeId;
	private Commit commit;
	private String url;
	private String htmlUrl;
	private String commentsUrl;
	private Author author;
	private Committer committer;
	private List<Parent> parents;
}

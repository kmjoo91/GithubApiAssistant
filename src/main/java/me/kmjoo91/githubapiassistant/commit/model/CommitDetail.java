package me.kmjoo91.githubapiassistant.commit.model;

import java.util.List;

import me.kmjoo91.githubapiassistant.common.model.Commit;
import me.kmjoo91.githubapiassistant.common.model.Committer;
import me.kmjoo91.githubapiassistant.common.model.Parent;
import me.kmjoo91.githubapiassistant.singlecommit.model.Author;

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

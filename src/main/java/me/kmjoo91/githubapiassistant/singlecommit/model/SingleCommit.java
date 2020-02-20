package me.kmjoo91.githubapiassistant.singlecommit.model;

import java.util.List;

import me.kmjoo91.githubapiassistant.common.model.Commit;
import me.kmjoo91.githubapiassistant.common.model.Committer;
import me.kmjoo91.githubapiassistant.common.model.Parent;

public class SingleCommit {
	private String sha;
	private String nodeId;
	private Commit commit;
	private String url;
	private String htmlUrl;
	private String commentsUrl;
	private Author author;
	private Committer committer;
	private List<Parent> parents;
	private Stats stats;
	private List<File> files;

	private class Stats {
		private int total;
		private int additions;
		private int deletions;
	}

	private class File {
		private String sha;
		private String filename;
		private String status;
		private int additions;
		private int deletions;
		private int changes;
		private String blobUrl;
		private String rawUrl;
		private String contentsUrl;
		private String patch;
	}
}

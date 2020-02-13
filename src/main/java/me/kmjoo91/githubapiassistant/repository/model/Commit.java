package me.kmjoo91.githubapiassistant.repository.model;

import java.time.LocalDateTime;

public class Commit {
	private Author author;
	private Committer committer;
	private String message;
	private Tree tree;
	private String url;
	private int commentCount;
	private Verification verification;

	private class Author {
		private String name;
		private String email;
		private LocalDateTime date;
	}

	private class Committer {
		private String name;
		private String email;
		private LocalDateTime date;
	}

	private class Tree {
		private String sha;
		private String url;
	}

	private class Verification {
		private boolean verified;
		private String reason;
		private String signature;
		private String payload;
	}
}

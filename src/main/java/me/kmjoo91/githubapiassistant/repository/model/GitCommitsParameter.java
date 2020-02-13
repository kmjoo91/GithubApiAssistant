package me.kmjoo91.githubapiassistant.repository.model;

import java.time.LocalDateTime;

public class GitCommitsParameter {
	//필수값
	private String owner;
	private String repository;

	//선택값
	private String sha;
	private String path;
	private String author;
	private LocalDateTime since;
	private LocalDateTime until;
}

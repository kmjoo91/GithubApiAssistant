package me.kmjoo91.githubapiassistant.commit.model;

import java.time.LocalDateTime;

import org.springframework.util.MultiValueMap;

import lombok.Setter;
import me.kmjoo91.githubapiassistant.common.annotation.PathVariableParameter;
import me.kmjoo91.githubapiassistant.common.annotation.QueryStringParameter;
import me.kmjoo91.githubapiassistant.common.model.QueryStringCreator;

@Setter
public class GitCommitsParameter implements QueryStringCreator {
	@PathVariableParameter
	private String owner;
	@PathVariableParameter
	private String repository;

	@QueryStringParameter
	private String sha;
	@QueryStringParameter
	private String path;
	@QueryStringParameter
	private String author;
	@QueryStringParameter
	private LocalDateTime since;
	@QueryStringParameter
	private LocalDateTime until;

	@Override
	public MultiValueMap<String, String> createQueryStringMap() {
		return null;
	}
}

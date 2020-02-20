package me.kmjoo91.githubapiassistant.singlecommit.model;

import lombok.Setter;
import me.kmjoo91.githubapiassistant.common.annotation.PathVariableParameter;

@Setter
public class SingleCommitParameter {
	@PathVariableParameter
	private String owner;
	@PathVariableParameter
	private String repository;
	@PathVariableParameter
	private String sha;
}

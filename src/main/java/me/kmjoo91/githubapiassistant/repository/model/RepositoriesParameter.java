package me.kmjoo91.githubapiassistant.repository.model;

import org.springframework.util.MultiValueMap;

import lombok.Setter;
import me.kmjoo91.githubapiassistant.common.annotation.QueryStringParameter;
import me.kmjoo91.githubapiassistant.common.model.QueryStringCreator;

@Setter
public class RepositoriesParameter implements QueryStringCreator {
	@QueryStringParameter
	private String visibility;
	@QueryStringParameter
	private String affiliation;
	@QueryStringParameter
	private String type;
	@QueryStringParameter
	private String sort;
	@QueryStringParameter
	private String direction;

	@Override
	public MultiValueMap<String, String> createQueryStringMap() {
		return null;
	}
}

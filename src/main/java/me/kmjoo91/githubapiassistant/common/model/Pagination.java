package me.kmjoo91.githubapiassistant.common.model;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.Setter;

@Setter
public class Pagination implements QueryStringCreator {
	private int page;
	private int perPage;

	@Override
	public MultiValueMap<String, String> createQueryStringMap() {
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.add("page", String.valueOf(page > 0 ? page : 1));
		multiValueMap.add("per_page", String.valueOf(perPage > 0 ? perPage : 30));
		return multiValueMap;
	}
}

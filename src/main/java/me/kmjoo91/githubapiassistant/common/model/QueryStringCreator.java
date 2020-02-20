package me.kmjoo91.githubapiassistant.common.model;

import org.springframework.util.MultiValueMap;

public interface QueryStringCreator {
	MultiValueMap<String, String> createQueryStringMap();
}

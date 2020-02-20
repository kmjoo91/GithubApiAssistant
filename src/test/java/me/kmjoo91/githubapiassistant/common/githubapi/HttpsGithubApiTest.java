package me.kmjoo91.githubapiassistant.common.githubapi;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.UriComponentsBuilder;

import me.kmjoo91.githubapiassistant.common.type.GithubApiType;

@ExtendWith(MockitoExtension.class)
class HttpsGithubApiTest {
	@Test
	void test() {
		URI uri = UriComponentsBuilder.newInstance()
			.scheme("https")
			.host("testhost.com")
			.path(GithubApiType.REPOSITORIES.getPath())
			.build()
			.encode()
			.toUri();

		System.out.println(uri);
	}

}
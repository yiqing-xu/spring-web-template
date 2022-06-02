package com.xyq.tweb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

@SpringBootTest
class SpringWebTemplateApplicationTests {

	@Test
	void contextLoads() {
		HttpMethod get = HttpMethod.valueOf("get".toUpperCase());
		System.out.println(get);
	}

}

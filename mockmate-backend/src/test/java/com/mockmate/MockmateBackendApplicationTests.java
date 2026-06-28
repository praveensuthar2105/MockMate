package com.mockmate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.task.scheduling.enabled=false",
		"app.jwt.secret=test-secret-key-that-is-at-least-thirty-two-bytes"
})
class MockmateBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}

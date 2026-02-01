package com.github.ryanribeiro.sensor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Tag("integration")
@SpringBootTest
class SensorApiApplicationTests {

	static final boolean useComposeDb = System.getenv("SPRING_DATASOURCE_URL") != null;
	static PostgreSQLContainer<?> postgres = null;

	static {
		if (!useComposeDb) {
			postgres = new PostgreSQLContainer<>("postgres:18")
					.withDatabaseName("postgres")
					.withUsername("postgres")
					.withPassword("123");
			postgres.start();
		}
	}

	@AfterAll
	static void teardown() {
		if (postgres != null) {
			try {
				if (postgres.isRunning()) {
					postgres.stop();
				}
			} catch (Exception ignored) {
			}
		}
	}

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		if (useComposeDb) {
			registry.add("spring.datasource.url", () -> System.getenv().getOrDefault("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:5432/postgres"));
			registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "postgres"));
			registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "123"));
			registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
			registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
		} else {
			registry.add("spring.datasource.url", postgres::getJdbcUrl);
			registry.add("spring.datasource.username", postgres::getUsername);
			registry.add("spring.datasource.password", postgres::getPassword);
			registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
			registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
		}
	}

	@Test
	void contextLoads() {
	}

}

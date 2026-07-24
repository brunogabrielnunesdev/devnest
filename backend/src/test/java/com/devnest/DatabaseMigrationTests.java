package com.devnest;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class DatabaseMigrationTests {

	@Test
	void appliesCommunityMigrationToCompatibleBaseSchema() throws Exception {
		String url = "jdbc:h2:mem:community-migration;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;"
				+ "INIT=CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE";
		try (var connection = DriverManager.getConnection(url, "sa", "")) {
			try (var statement = connection.createStatement()) {
				statement.execute("create table users (id uuid primary key)");
				statement.execute("create table projects (id uuid primary key)");
				statement.execute("create table courses (id uuid primary key)");
			}

			ScriptUtils.executeSqlScript(
					connection,
					new ClassPathResource("db/migration/V7__create_community_forums_and_posts.sql")
			);

			try (var statement = connection.createStatement();
					var result = statement.executeQuery("select count(*) from information_schema.tables "
							+ "where table_name in ('community_forums', 'community_tags', "
							+ "'community_posts', 'community_post_tags')")) {
				assertThat(result.next()).isTrue();
				assertThat(result.getInt(1)).isEqualTo(4);
			}
		}
	}
}

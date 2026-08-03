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
			ScriptUtils.executeSqlScript(
					connection,
					new ClassPathResource("db/migration/V8__create_community_comments.sql")
			);
			ScriptUtils.executeSqlScript(
					connection,
					new ClassPathResource("db/migration/V9__create_community_reactions.sql")
			);
			ScriptUtils.executeSqlScript(
					connection,
					new ClassPathResource("db/migration/V10__create_community_user_relations.sql")
			);
			ScriptUtils.executeSqlScript(
					connection,
					new ClassPathResource("db/migration/V11__create_community_rate_limit_events.sql")
			);
			ScriptUtils.executeSqlScript(
					connection,
					new ClassPathResource("db/migration/V12__create_community_reports.sql")
			);
			ScriptUtils.executeSqlScript(
					connection,
					new ClassPathResource("db/migration/V13__create_community_moderation.sql")
			);

			try (var statement = connection.createStatement();
					var result = statement.executeQuery("select count(*) from information_schema.tables "
							+ "where table_name in ('community_forums', 'community_tags', "
							+ "'community_posts', 'community_post_tags', 'community_comments', "
							+ "'community_reactions', 'community_user_blocks', 'community_user_mutes', "
							+ "'community_rate_limit_events', 'community_reports', "
							+ "'community_moderation_cases', 'community_moderation_actions')")) {
				assertThat(result.next()).isTrue();
				assertThat(result.getInt(1)).isEqualTo(12);
			}
		}
	}
}

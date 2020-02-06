package org.group22.utilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.geom.Point2D;

public class HelpersTest {
	@Nested
	@DisplayName("Tests the generate ID function")
	class GenerateIDTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        	LocalDateTime now = LocalDateTime.now();
        	assertEquals("id_" + dtf.format(now), Helpers.generateId("id"));
		}

		@Test
		@DisplayName("Basic fail test")
		void basicFailTest() {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        	LocalDateTime now = LocalDateTime.now();
        	assertNotEquals("_" + dtf.format(now), Helpers.generateId("id"));
		}
	}

	@Nested
	@DisplayName("Tests the getHeadCommitId function")
	class getHeadCommitIdTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			JSONObject json = new JSONObject();
			JSONObject id   = new JSONObject();
			id.put("id", "id");
			json.put("head_commit", id); 
        	assertEquals("id", Helpers.getHeadCommitId(json));
		}
	}

	@Nested
	@DisplayName("Tests the get Author function")
	class getAuthorTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			JSONObject json = new JSONObject();
			JSONObject author   = new JSONObject();
			author.put("name", "Author_Name");
			json.put("pusher", author); 
        	assertEquals("Author_Name", Helpers.getAuthor(json));
		}
	}

	@Nested
	@DisplayName("Tests the get Repository Name function")
	class geRepositoryNameTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			JSONObject json = new JSONObject();
			JSONObject repo   = new JSONObject();
			repo.put("name", "Repo_Name");
			json.put("repository", repo); 
        	assertEquals("Repo_Name", Helpers.getRepositoryName(json));
		}
	}

	@Nested
	@DisplayName("Tests the get Clone URL function")
	class getHeadCloneURLTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			JSONObject json = new JSONObject();
			JSONObject repo   = new JSONObject();
			repo.put("clone_url", "url");
			json.put("repository", repo); 
        	assertEquals("url", Helpers.getCloneURL(json));
		}
	}
}

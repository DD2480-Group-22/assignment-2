package org.group22.utilities;


import org.group22.ci.AWSFileUploader;

import org.group22.utilities.Configuration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import java.util.*;

import java.io.File;


import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;


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
	@DisplayName("Tests the isPushEvent function")
	class isPushEventTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			JSONObject json = new JSONObject();
			JSONObject id   = new JSONObject();
			id.put("id", "id");
			json.put("head_commit", id); 
        	assertTrue(Helpers.isPushEvent(json));
        	assertFalse(Helpers.isPushEvent(id));
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
	@DisplayName("Tests the get Branch function")
	class getBranchTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			JSONObject json = new JSONObject();
			json.put("ref", "refs/heads/branch_name");
        	assertEquals("branch_name", Helpers.getBranch(json));
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

	@Nested
	@DisplayName("Tests the setUpConfiguration function")
	class setUpConfigurationTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			String[] args1 = {"arg1", "arg2", "arg3"};
			String[] args2 = {"", "arg2"};
			String[] args3 = {"arg1", ""}; 
        	assertThrows(MissingResourceException.class, () -> {Helpers.setUpConfiguration(args1);});
        	assertThrows(IllegalArgumentException.class, () -> {Helpers.setUpConfiguration(args2);});
        	assertThrows(IllegalArgumentException.class, () -> {Helpers.setUpConfiguration(args3);});
		}
	}

	@Nested
	@DisplayName("Tests the updatePreviousBuilds function")
	class updatePreviousBuildsTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			Set<String> set = new HashSet();
			String id = "id";
			set.add(id);
			Helpers.updatePreviousBuilds("id");
			assertEquals(set, Configuration.PREVIOUS_BUILDS);
		}
	}

	@Nested
	@DisplayName("Tests the clean up function")
	class cleanUpTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			String id = "folder_with_stuff";
			
			File directory = new File("git/"+id);
			directory.mkdirs();
			
			assertTrue(directory.exists());
			
			Helpers.cleanUp(id);
			
			assertFalse(directory.exists());
		}
	}
	
	@Nested
	@DisplayName("Tests the generate index function")
	class generateIndexTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			StringBuilder stringBuilderList = new StringBuilder();
	        stringBuilderList.append("<!DOCTYPE html> <html lang=\"en\">");
	        stringBuilderList.append("<head>" + "<meta charset=\"UTF-8\">" + "<title>CI Server</title>" + "</head>");
	        stringBuilderList.append("<body>");
	        stringBuilderList.append("<h1>Index Group 22 - CI Server</h1>");
	        stringBuilderList.append("<h2>List of previous builds on the CI server</h2>");
	        stringBuilderList.append("<ul>");
	        stringBuilderList.append("</ul>");
	        stringBuilderList.append("</body>");
	        stringBuilderList.append("</html>");
	        String test = stringBuilderList.toString();
			assertEquals(test, Helpers.generateIndex());

		}
	}

	@Nested
	@DisplayName("Tests the reportAddress function")
	class reportAdressTest {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			String test_string = "https://" + Configuration.BUCKET_NAME + ".s3." +
                Configuration.S3_BUCKET_REGION + ".amazonaws.com/reports/test_id.txt"; 
        	assertEquals(test_string, Helpers.reportAddress("test_id"));
		}
	}
}

package org.group22.utilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
	}
	
	@Nested
	@DisplayName("Tests the Head Commit ID function")
	class GetHeadCommitIdTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the Head Commit ID function")
	class GetBranchTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the get author from json file function")
	class GetAuthorTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the get repository from json file function")
	class GetRepositoryTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the get clone url from json file function")
	class GetCloneURLTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the function that sets configurations")
	class SetupConfigurationsTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the function that adds reports about previous builds")
	class PreviousBuildTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the file and directory clean up function")
	class CleanUpTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the index generating function")
	class GenerateIndexTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
	
	@Nested
	@DisplayName("Tests the address report function")
	class ReportAddressTests {
		@Test
		@DisplayName("Basic test")
		void basicTest() {
			
		}
	}
}

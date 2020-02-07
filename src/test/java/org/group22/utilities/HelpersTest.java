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
}

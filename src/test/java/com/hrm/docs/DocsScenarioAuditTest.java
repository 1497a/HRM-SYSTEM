package com.hrm.docs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocsScenarioAuditTest {

    @Test
    @DisplayName("docs: file hrm_smaple_data.sql ton tai cho cac tai lieu scenario")
    void sampleDataAlias_exists() {
        assertTrue(Files.exists(Path.of("sql", "hrm_smaple_data.sql")));
    }
}

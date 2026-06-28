package com.mockmate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DsaProblemServiceTest {

    private DsaProblemService dsaProblemService;

    @BeforeEach
    void setUp() {
        dsaProblemService = new DsaProblemService(
                Optional.empty(),
                null,
                new ObjectMapper()
        );
    }

    @Test
    void testRepairTruncatedJson_ValidJson() {
        String json = "{\"title\":\"Two Sum\",\"difficulty\":\"EASY\"}";
        String repaired = dsaProblemService.repairTruncatedJson(json);
        assertEquals(json, repaired);
    }

    @Test
    void testRepairTruncatedJson_TruncatedInKey() {
        String json = "{\"title\":\"Two Sum\",\"diffi";
        String repaired = dsaProblemService.repairTruncatedJson(json);
        // Should truncate back to the end of the previous field and close correctly
        assertEquals("{\"title\":\"Two Sum\"}", repaired);
    }

    @Test
    void testRepairTruncatedJson_TruncatedInValueString() {
        String json = "{\"title\":\"Two Sum\",\"description\":\"Given an array of integers nums and an";
        String repaired = dsaProblemService.repairTruncatedJson(json);
        // It's truncated within a string value, it should be closed as "Given an array of integers nums and an"
        // and closed correctly with }
        assertEquals("{\"title\":\"Two Sum\",\"description\":\"Given an array of integers nums and an\"}", repaired);
    }

    @Test
    void testRepairTruncatedJson_TruncatedInArray() {
        String json = "{\"title\":\"Two Sum\",\"constraints\":[\"1 <= nums.length\",\"2 <= target\",\"";
        String repaired = dsaProblemService.repairTruncatedJson(json);
        // Truncated at the start of the third element. It should discard the empty third element.
        assertEquals("{\"title\":\"Two Sum\",\"constraints\":[\"1 <= nums.length\",\"2 <= target\"]}", repaired);
    }

    @Test
    void testRepairTruncatedJson_TruncatedInsideArrayString() {
        String json = "{\"title\":\"Two Sum\",\"constraints\":[\"1 <= nums.length\",\"2 <= tar";
        String repaired = dsaProblemService.repairTruncatedJson(json);
        // Truncated inside the second element of the array. It should preserve the partial string element.
        assertEquals("{\"title\":\"Two Sum\",\"constraints\":[\"1 <= nums.length\",\"2 <= tar\"]}", repaired);
    }

    @Test
    void testRepairTruncatedJson_TruncatedBetweenObjectsInArray() {
        String json = "{\"title\":\"Two Sum\",\"testCases\":[{\"input\":\"1\",\"output\":\"2\"},{\"inpu";
        String repaired = dsaProblemService.repairTruncatedJson(json);
        // Truncated at the start of the second object in the array.
        // It should discard the second object and close the array/object.
        assertEquals("{\"title\":\"Two Sum\",\"testCases\":[{\"input\":\"1\",\"output\":\"2\"}]}", repaired);
    }

    @Test
    void testRepairTruncatedJson_NaivelyRepairsIfNoPrefixSucceeds() {
        String json = "Invalid JSON that can't be trimmed easily {";
        String repaired = dsaProblemService.repairTruncatedJson(json);
        // Should fall back to naive repair
        assertTrue(repaired.endsWith("}"));
    }
}

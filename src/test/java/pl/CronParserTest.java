package pl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CronParserTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testFullCronExpression() {
        String cronExpression = "*/15 0 1,15 * 1-5 /usr/bin/find";
        String[] expectedOutput = {
                "minute        0 15 30 45",
                "hour          0",
                "day of month  1 15",
                "month         1 2 3 4 5 6 7 8 9 10 11 12",
                "day of week   1 2 3 4 5",
                "command       /usr/bin/find"
        };

        CronParser.parseCronExpression(cronExpression);
        assertOutputContains(expectedOutput);
    }

    @Test
    void testInvalidCronExpression() {
        String cronExpression = "*/15 0 1,15 * 1-5";
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> CronParser.parseCronExpression(cronExpression));
        assertEquals("Invalid cron expression. Expected 5 time fields and 1 command.", thrown.getMessage());
    }

    @Test
    void testExpandFieldWithStep() {
        String result = CronParser.expandField("*/15", CronParser.CronFieldType.MINUTE);
        assertEquals("0 15 30 45", result);
    }

    @Test
    void testExpandFieldWithList() {
        String result = CronParser.expandField("1,15", CronParser.CronFieldType.DAY_OF_MONTH);
        assertEquals("1 15", result);
    }

    @Test
    void testExpandFieldWithRange() {
        String result = CronParser.expandField("1-5", CronParser.CronFieldType.DAY_OF_WEEK);
        assertEquals("1 2 3 4 5", result);
    }

    @Test
    void testExpandFieldWithNames() {
        String result = CronParser.expandField("JAN-MAR", CronParser.CronFieldType.MONTH);
        assertEquals("1 2 3", result);
    }

    @Test
    void testHandleSingleValueWithName() {
        String result = CronParser.expandField("JAN", CronParser.CronFieldType.MONTH);
        assertEquals("1", result);
    }

    @Test
    void testHandleSingleValueWithNumber() {
        String result = CronParser.expandField("5", CronParser.CronFieldType.MINUTE);
        assertEquals("5", result);
    }

    @Test
    void testHandleInvalidNumericValue() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> CronParser.expandField("60", CronParser.CronFieldType.MINUTE));
        assertEquals("Field value out of range: 60", thrown.getMessage());
    }

    @Test
    void testHandleInvalidFormat() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> CronParser.expandField("5-1", CronParser.CronFieldType.MINUTE));
        assertEquals("Invalid range: 5-1", thrown.getMessage());
    }

    @Test
    void testExpandFieldWithInvalidStep() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> CronParser.expandField("*/-1", CronParser.CronFieldType.MINUTE));
        assertEquals("Step value must be greater than 0: */-1", thrown.getMessage());
    }

    @Test
    void testExpandFieldWithInvalidRange() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> CronParser.expandField("5-1", CronParser.CronFieldType.HOUR));
        assertEquals("Invalid range: 5-1", thrown.getMessage());
    }

    @Test
    void testExpandFieldWithInvalidValue() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> CronParser.expandField("100", CronParser.CronFieldType.MINUTE));
        assertEquals("Field value out of range: 100", thrown.getMessage());
    }

    @Test
    void testExpandFieldWithValidRange() {
        String result = CronParser.expandField("1-5", CronParser.CronFieldType.HOUR);
        assertEquals("1 2 3 4 5", result);
    }


    private void assertOutputContains(String[] expectedOutput) {
        String output = outContent.toString().trim();
        String[] outputLines = output.split("\\r?\\n");
        for (String expectedLine : expectedOutput) {
            assertTrue(Arrays.asList(outputLines).contains(expectedLine));
        }
    }
}

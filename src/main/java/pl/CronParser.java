package pl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CronParser {

    private static final String OUTPUT_FORMAT = "%-14s%s%n";

    public static void main(String[] args) {
        // Ensure a single argument is passed
        if (args.length != 1) {
            System.err.println("Usage: java CronParser \"<cron expression>\"");
            System.exit(1);
        }

        // Parse and process the cron expression
        String cronExpression = args[0];
        parseCronExpression(cronExpression);
    }

    public static void parseCronExpression(String cronExpression) {
        // Split the cron expression into its components
        String[] parts = cronExpression.split("\\s+");

        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid cron expression. Expected 5 time fields and 1 command.");
        }

        String minute = parts[0];
        String hour = parts[1];
        String dayOfMonth = parts[2];
        String month = parts[3];
        String dayOfWeek = parts[4];
        String command = parts[5];

        // Print the expanded fields
        System.out.printf(OUTPUT_FORMAT, "minute", expandField(minute, CronFieldType.MINUTE));
        System.out.printf(OUTPUT_FORMAT, "hour", expandField(hour, CronFieldType.HOUR));
        System.out.printf(OUTPUT_FORMAT, "day of month", expandField(dayOfMonth, CronFieldType.DAY_OF_MONTH));
        System.out.printf(OUTPUT_FORMAT, "month", expandField(month, CronFieldType.MONTH));
        System.out.printf(OUTPUT_FORMAT, "day of week", expandField(dayOfWeek, CronFieldType.DAY_OF_WEEK));
        System.out.printf(OUTPUT_FORMAT, "command", command);
    }

    public static String expandField(String field, CronFieldType fieldType) {
        if (field.equals("*")) {
            return rangeToString(fieldType.min, fieldType.max);
        } else if (field.contains("/")) {
            return expandStep(field, fieldType);
        } else if (field.contains(",")) {
            return expandList(field, fieldType.names);
        } else if (field.contains("-")) {
            return expandRange(field, fieldType.names);
        } else {
            return handleSingleValue(field, fieldType);
        }
    }

    private static String handleSingleValue(String field, CronFieldType fieldType) {
        if (fieldType.names != null && fieldType.names.contains(field.toUpperCase(Locale.getDefault()))) {
            return String.valueOf(fieldType.names.indexOf(field.toUpperCase(Locale.getDefault())) + fieldType.min);
        } else {
            return validateNumericField(field, fieldType);
        }
    }

    private static String validateNumericField(String field, CronFieldType fieldType) {
        try {
            int value = Integer.parseInt(field);
            if (value < fieldType.min || value > fieldType.max) {
                throw new IllegalArgumentException("Field value out of range: " + field);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid field value: " + field);
        }
        return field;
    }

    private static String rangeToString(int min, int max) {
        return range(min, max).stream().map(String::valueOf).collect(Collectors.joining(" "));
    }

    private static List<Integer> range(int min, int max) {
        List<Integer> result = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            result.add(i);
        }
        return result;
    }

    private static String expandStep(String field, CronFieldType fieldType) {
        String[] parts = field.split("/");
        int step = Integer.parseInt(parts[1]);
        if (step <= 0) {
            throw new IllegalArgumentException("Step value must be greater than 0: " + field);
        }

        int start = fieldType.min;
        int end = fieldType.max;

        if (!parts[0].equals("*")) {
            String[] rangeParts = parts[0].split("-");
            start = Integer.parseInt(rangeParts[0]);
            end = Integer.parseInt(rangeParts[1]);
            if (start > end) {
                throw new IllegalArgumentException("Invalid range: " + field);
            }
        }

        List<Integer> result = new ArrayList<>();
        for (int i = start; i <= end; i += step) {
            result.add(i);
        }

        return result.stream().map(String::valueOf).collect(Collectors.joining(" "));
    }

    private static String expandList(String field, List<String> names) {
        return Arrays.stream(field.split(","))
                .map(value -> names != null && names.contains(value.toUpperCase(Locale.getDefault())) ?
                        String.valueOf(names.indexOf(value.toUpperCase(Locale.getDefault())) + 1) : value)
                .collect(Collectors.joining(" "));
    }

    private static String expandRange(String field, List<String> names) {
        String[] parts = field.split("-");
        int start = names != null && names.contains(parts[0].toUpperCase(Locale.getDefault())) ?
                names.indexOf(parts[0].toUpperCase(Locale.getDefault())) + 1 : Integer.parseInt(parts[0]);
        int end = names != null && names.contains(parts[1].toUpperCase(Locale.getDefault())) ?
                names.indexOf(parts[1].toUpperCase(Locale.getDefault())) + 1 : Integer.parseInt(parts[1]);

        if (start > end) {
            throw new IllegalArgumentException("Invalid range: " + field);
        }

        return range(start, end).stream().map(String::valueOf).collect(Collectors.joining(" "));
    }

    public enum CronFieldType {
        MINUTE(0, 59, null),
        HOUR(0, 23, null),
        DAY_OF_MONTH(1, 31, null),
        MONTH(1, 12, Arrays.asList("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")),
        DAY_OF_WEEK(0, 6, Arrays.asList("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"));

        final int min;
        final int max;
        final List<String> names;

        CronFieldType(int min, int max, List<String> names) {
            this.min = min;
            this.max = max;
            this.names = names;
        }
    }
}

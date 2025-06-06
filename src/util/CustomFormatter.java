package util;

import java.time.format.DateTimeFormatter;

public class CustomFormatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static DateTimeFormatter getFormatter() {
        return formatter;
    }
}

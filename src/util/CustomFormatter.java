package util;

import java.time.format.DateTimeFormatter;

public class CustomFormatter {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public DateTimeFormatter getFormatter() {
        return formatter;
    }
}

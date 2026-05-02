package com.lulibrisync.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DisplayFormatUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);

    private DisplayFormatUtils() {
    }

    public static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME_FORMATTER.format(value);
    }

    public static String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMATTER.format(value);
    }
}

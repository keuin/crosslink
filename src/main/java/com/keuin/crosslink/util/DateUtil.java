package com.keuin.crosslink.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Copied from legacy BungeeCross codebase.
 */
public class DateUtil {

    private static final DateTimeFormatter MONTH_DAY_HOUR_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("MM.dd HH:mm");

    public static String getMonthDayHourMinuteString(LocalDateTime localDateTime) {
        return localDateTime.format(MONTH_DAY_HOUR_MINUTE_FORMATTER);
    }

    public static String getOffsetString(LocalDateTime localDateTime) {
        // TODO: test this
        LocalDateTime currentTime = LocalDateTime.now();
        long seconds = currentTime.toEpochSecond(ZoneOffset.UTC)
                - localDateTime.toEpochSecond(ZoneOffset.UTC);
        String direction;
        if (seconds < 0)
            direction = "later";
        else
            direction = "ago";

        StringBuilder absOffset = new StringBuilder();
        String[] unitNames = new String[]{"y", "mo", "d", "h", "m", "s"};
        long[] unitSeconds = new long[]{365 * 24 * 60 * 60, 30 * 24 * 60 * 60, 24 * 60 * 60, 60 * 60, 60, 1};
        for (int i = 0; i < unitNames.length; i++) {
            long c = seconds / unitSeconds[i];
            seconds %= unitSeconds[i];
            if (c > 0)
                absOffset.append(c).append(unitNames[i]).append(" ");
        }

        if (absOffset.length() == 0)
            return "now";
        else
            return absOffset + direction; // e.g. 1m3s ago
    }
}


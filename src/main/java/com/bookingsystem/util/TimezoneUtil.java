package com.bookingsystem.util;

import com.bookingsystem.exception.Exceptions;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Timezone utility.
 *
 * Design decisions:
 * - All timestamps are stored in the database as UTC (Instant).
 * - Teachers supply session times as LocalDateTime in their own timezone.
 * - Parents receive session times formatted in their own timezone.
 * - ZoneId is validated eagerly to return clear error messages.
 */
public final class TimezoneUtil {

    private TimezoneUtil() {}

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Set<String> VALID_TIMEZONES = ZoneId.getAvailableZoneIds();

    /**
     * Validate an IANA timezone string, throw if invalid.
     */
    public static ZoneId validateAndGet(String timezone) {
        if (!VALID_TIMEZONES.contains(timezone)) {
            throw new Exceptions.InvalidTimezoneException(timezone);
        }
        return ZoneId.of(timezone);
    }

    /**
     * Convert a teacher's local date-time to a UTC Instant for storage.
     *
     * @param localDateTime  date-time in the teacher's timezone
     * @param teacherTimezone IANA timezone string
     * @return UTC Instant
     */
    public static Instant toUtc(LocalDateTime localDateTime, String teacherTimezone) {
        ZoneId zone = validateAndGet(teacherTimezone);
        return localDateTime.atZone(zone).toInstant();
    }

    /**
     * Format a UTC Instant as a human-readable string in the viewer's local timezone.
     *
     * @param utcInstant  the UTC timestamp from DB
     * @param viewerTimezone IANA timezone string of the viewer (teacher or parent)
     * @return formatted string, e.g. "2025-06-07 18:00:00"
     */
    public static String formatInTimezone(Instant utcInstant, String viewerTimezone) {
        ZoneId zone = validateAndGet(viewerTimezone);
        ZonedDateTime local = utcInstant.atZone(zone);
        return local.format(DISPLAY_FORMATTER);
    }

    /**
     * Check whether two time intervals overlap.
     * Uses the standard half-open interval overlap condition:
     *   start1 < end2 AND end1 > start2
     */
    public static boolean overlaps(Instant start1, Instant end1, Instant start2, Instant end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}

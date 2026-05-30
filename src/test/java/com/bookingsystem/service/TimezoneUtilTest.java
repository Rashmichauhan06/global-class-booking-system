package com.bookingsystem.service;

import com.bookingsystem.exception.Exceptions;
import com.bookingsystem.util.TimezoneUtil;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class TimezoneUtilTest {

    @Test
    void toUtc_convertsCorrectly() {
        // 6 PM New York (UTC-5 in winter = UTC+0 at 23:00)
        LocalDateTime nyTime = LocalDateTime.of(2025, 6, 7, 18, 0, 0);
        Instant utc = TimezoneUtil.toUtc(nyTime, "America/New_York");

        // June 7 is EDT = UTC-4, so 18:00 EDT = 22:00 UTC
        ZonedDateTime expected = ZonedDateTime.of(nyTime, ZoneId.of("America/New_York"));
        assertThat(utc).isEqualTo(expected.toInstant());
    }

    @Test
    void formatInTimezone_showsLocalTime() {
        // UTC 22:00 should appear as 18:00 in New York (EDT = UTC-4)
        Instant utc = Instant.parse("2025-06-07T22:00:00Z");
        String formatted = TimezoneUtil.formatInTimezone(utc, "America/New_York");
        assertThat(formatted).isEqualTo("2025-06-07 18:00:00");
    }

    @Test
    void formatInTimezone_kolkata() {
        // UTC 22:00 = IST 03:30 next day (UTC+5:30)
        Instant utc = Instant.parse("2025-06-07T22:00:00Z");
        String formatted = TimezoneUtil.formatInTimezone(utc, "Asia/Kolkata");
        assertThat(formatted).isEqualTo("2025-06-08 03:30:00");
    }

    @Test
    void validateAndGet_throwsOnInvalidTimezone() {
        assertThatThrownBy(() -> TimezoneUtil.validateAndGet("Not/ATimezone"))
                .isInstanceOf(Exceptions.InvalidTimezoneException.class)
                .hasMessageContaining("Not/ATimezone");
    }

    @Test
    void overlaps_detectsOverlap() {
        Instant a = Instant.parse("2025-06-07T17:00:00Z");
        Instant b = Instant.parse("2025-06-07T18:00:00Z");
        Instant c = Instant.parse("2025-06-07T17:30:00Z");
        Instant d = Instant.parse("2025-06-07T18:30:00Z");

        // [17:00-18:00] overlaps [17:30-18:30] ✓
        assertThat(TimezoneUtil.overlaps(a, b, c, d)).isTrue();
    }

    @Test
    void overlaps_noOverlapForAdjacentIntervals() {
        Instant a = Instant.parse("2025-06-07T17:00:00Z");
        Instant b = Instant.parse("2025-06-07T18:00:00Z");
        Instant c = Instant.parse("2025-06-07T18:00:00Z");
        Instant d = Instant.parse("2025-06-07T19:00:00Z");

        // [17:00-18:00] and [18:00-19:00] are adjacent, NOT overlapping
        assertThat(TimezoneUtil.overlaps(a, b, c, d)).isFalse();
    }

    @Test
    void overlaps_noOverlapForDistinctIntervals() {
        Instant a = Instant.parse("2025-06-07T17:00:00Z");
        Instant b = Instant.parse("2025-06-07T18:00:00Z");
        Instant c = Instant.parse("2025-06-07T19:00:00Z");
        Instant d = Instant.parse("2025-06-07T20:00:00Z");

        assertThat(TimezoneUtil.overlaps(a, b, c, d)).isFalse();
    }
}

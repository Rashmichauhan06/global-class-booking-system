package com.bookingsystem.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * All API response shapes, grouped here for easy reference.
 */
public final class Responses {

    private Responses() {}

    // ── Teacher ───────────────────────────────────────────────────────────────

    public record TeacherResponse(
            Long id,
            String name,
            String email,
            String timezone,
            Instant createdAt
    ) {}

    // ── Parent ────────────────────────────────────────────────────────────────

    public record ParentResponse(
            Long id,
            String name,
            String email,
            String timezone,
            Instant createdAt
    ) {}

    // ── Course ────────────────────────────────────────────────────────────────

    public record CourseResponse(
            Long id,
            String title,
            String description,
            Instant createdAt,
            Instant updatedAt
    ) {}

    // ── Session ───────────────────────────────────────────────────────────────

    /**
     * Session times shown in UTC — the caller specifies a timezone via path variable
     * and the service returns the localised version in the fields below.
     */
    public record SessionResponse(
            Long id,
            Long offeringId,
            Long teacherId,
            // ISO-8601 strings in the viewer's local timezone
            String startTimeLocal,
            String endTimeLocal,
            String timezone,
            // Always included for reference / audit
            Instant startTimeUtc,
            Instant endTimeUtc,
            String status
    ) {}

    // ── Offering ──────────────────────────────────────────────────────────────

    public record OfferingResponse(
            Long id,
            CourseResponse course,
            TeacherResponse teacher,
            String title,
            String description,
            Integer maxStudents,
            String status,
            List<SessionResponse> sessions,
            Instant createdAt
    ) {}

    // ── Booking ───────────────────────────────────────────────────────────────

    public record BookingResponse(
            Long id,
            Long parentId,
            OfferingResponse offering,
            String status,
            Instant bookedAt
    ) {}
}

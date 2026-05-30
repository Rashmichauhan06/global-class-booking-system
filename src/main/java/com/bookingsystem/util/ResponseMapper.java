package com.bookingsystem.util;

import com.bookingsystem.dto.response.Responses.*;
import com.bookingsystem.entity.*;

import java.util.List;

/**
 * Stateless mapper from entities to response DTOs.
 * Kept separate from entities to maintain a clean domain model.
 */
public final class ResponseMapper {

    private ResponseMapper() {}

    public static TeacherResponse toTeacherResponse(Teacher t) {
        return new TeacherResponse(t.getId(), t.getName(), t.getEmail(), t.getTimezone(), t.getCreatedAt());
    }

    public static ParentResponse toParentResponse(Parent p) {
        return new ParentResponse(p.getId(), p.getName(), p.getEmail(), p.getTimezone(), p.getCreatedAt());
    }

    public static CourseResponse toCourseResponse(Course c) {
        return new CourseResponse(c.getId(), c.getTitle(), c.getDescription(),   c.getCreatedAt(),
                c.getUpdatedAt());
    }

    /**
     * Map a session for a specific viewer (teacher or parent).
     * Times are displayed in the viewer's local timezone.
     *
     * @param session       the session entity (UTC timestamps)
     * @param viewerTimezone IANA timezone of whoever is looking at this session
     */
    public static SessionResponse toSessionResponse(Session session, String viewerTimezone) {
        return new SessionResponse(
                session.getId(),
                session.getOffering().getId(),
                session.getTeacher().getId(),
                TimezoneUtil.formatInTimezone(session.getStartTimeUtc(), viewerTimezone),
                TimezoneUtil.formatInTimezone(session.getEndTimeUtc(), viewerTimezone),
                viewerTimezone,
                session.getStartTimeUtc(),
                session.getEndTimeUtc(),
                session.getStatus().name()
        );
    }

    /**
     * Map an offering, rendering sessions in the viewer's timezone.
     */
    public static OfferingResponse toOfferingResponse(Offering offering, String viewerTimezone) {
        List<SessionResponse> sessions = offering.getSessions().stream()
                .map(s -> toSessionResponse(s, viewerTimezone))
                .toList();

        return new OfferingResponse(
                offering.getId(),
                toCourseResponse(offering.getCourse()),
                toTeacherResponse(offering.getTeacher()),
                offering.getTitle(),
                offering.getDescription(),
                offering.getMaxStudents(),
                offering.getStatus().name(),
                sessions,
                offering.getCreatedAt()
        );
    }

    public static BookingResponse toBookingResponse(Booking booking, String viewerTimezone) {
        return new BookingResponse(
                booking.getId(),
                booking.getParent().getId(),
                toOfferingResponse(booking.getOffering(), viewerTimezone),
                booking.getStatus().name(),
                booking.getBookedAt()
        );
    }
}

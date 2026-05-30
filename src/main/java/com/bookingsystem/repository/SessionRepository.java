package com.bookingsystem.repository;

import com.bookingsystem.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByOfferingIdOrderByStartTimeUtcAsc(Long offeringId);

    /**
     * Find all SCHEDULED sessions belonging to offerings the parent has CONFIRMED bookings for.
     * Used to get the parent's locked time windows.
     */
    @Query("""
        SELECT s FROM Session s
        JOIN s.offering o
        JOIN o.bookings b
        WHERE b.parent.id = :parentId
          AND b.status = 'CONFIRMED'
          AND s.status = 'SCHEDULED'
        ORDER BY s.startTimeUtc ASC
        """)
    List<Session> findBookedSessionsForParent(@Param("parentId") Long parentId);

    /**
     * Core conflict-detection query.
     *
     * Checks whether ANY session from the target offering overlaps with ANY session
     * already booked by the parent.
     *
     * Two time intervals [A_start, A_end) and [B_start, B_end) overlap when:
     *   A_start < B_end  AND  A_end > B_start
     *
     * We exclude the current offering itself (in case of re-query scenarios).
     */
    @Query("""
        SELECT COUNT(s) FROM Session s
        JOIN s.offering o
        JOIN o.bookings b
        WHERE b.parent.id  = :parentId
          AND b.status      = 'CONFIRMED'
          AND s.status      = 'SCHEDULED'
          AND o.id         != :targetOfferingId
          AND EXISTS (
              SELECT ts FROM Session ts
              WHERE ts.offering.id = :targetOfferingId
                AND ts.status      = 'SCHEDULED'
                AND ts.startTimeUtc < s.endTimeUtc
                AND ts.endTimeUtc   > s.startTimeUtc
          )
        """)
    long countConflictingSessions(
            @Param("parentId") Long parentId,
            @Param("targetOfferingId") Long targetOfferingId);

    /**
     * Fetch sessions for a specific teacher after a given time (for upcoming view).
     */
    @Query("""
        SELECT s FROM Session s
        WHERE s.teacher.id    = :teacherId
          AND s.startTimeUtc  > :from
          AND s.status        = 'SCHEDULED'
        ORDER BY s.startTimeUtc ASC
        """)
    List<Session> findUpcomingByTeacher(
            @Param("teacherId") Long teacherId,
            @Param("from") Instant from);
}

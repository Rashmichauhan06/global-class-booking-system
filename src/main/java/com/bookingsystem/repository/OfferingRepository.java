package com.bookingsystem.repository;

import com.bookingsystem.entity.Offering;
import com.bookingsystem.entity.OfferingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {

    /**
     * All active offerings for a specific teacher that have at least one upcoming session.
     */
    @Query("""
        SELECT DISTINCT o FROM Offering o
        JOIN FETCH o.course
        JOIN FETCH o.teacher
        WHERE o.teacher.id = :teacherId
          AND o.status = :status
        ORDER BY o.createdAt DESC
        """)
    List<Offering> findByTeacherIdAndStatus(
            @Param("teacherId") Long teacherId,
            @Param("status") OfferingStatus status);

    /**
     * All ACTIVE offerings that have at least one session in the future.
     * Used for the parent "browse available offerings" endpoint.
     */
    @Query("""
        SELECT DISTINCT o FROM Offering o
        JOIN FETCH o.course
        JOIN FETCH o.teacher
        WHERE o.status = 'ACTIVE'
          AND EXISTS (
              SELECT s FROM Session s
              WHERE s.offering = o
                AND s.status = 'SCHEDULED'
                AND s.startTimeUtc > :now
          )
        ORDER BY o.createdAt DESC
        """)
    List<Offering> findAvailableOfferings(@Param("now") Instant now);
}

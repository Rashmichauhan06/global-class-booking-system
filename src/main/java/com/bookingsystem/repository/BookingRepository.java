package com.bookingsystem.repository;

import com.bookingsystem.entity.Booking;
import com.bookingsystem.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByParentIdAndOfferingId(Long parentId, Long offeringId);

    Optional<Booking> findByParentIdAndOfferingId(Long parentId, Long offeringId);

    /**
     * All bookings for a parent with full offering + course + sessions eagerly loaded
     * to avoid N+1 in the response serialization.
     */
    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.offering o
        JOIN FETCH o.course
        JOIN FETCH o.teacher
        WHERE b.parent.id = :parentId
          AND b.status    = :status
        ORDER BY b.bookedAt DESC
        """)
    List<Booking> findByParentIdAndStatus(
            @Param("parentId") Long parentId,
            @Param("status") BookingStatus status);

    /**
     * Count active (CONFIRMED) bookings for an offering — used to enforce capacity.
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.offering.id = :offeringId
          AND b.status      = 'CONFIRMED'
        """)
    long countConfirmedBookings(@Param("offeringId") Long offeringId);
}

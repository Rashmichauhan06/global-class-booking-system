package com.bookingsystem.service;

import com.bookingsystem.dto.request.BookOfferingRequest;
import com.bookingsystem.dto.request.CreateParentRequest;
import com.bookingsystem.dto.response.Responses.*;

import java.util.List;

public interface ParentService {

    ParentResponse createParent(CreateParentRequest request);

    /**
     * Return all ACTIVE offerings that have at least one future session.
     * Session times are rendered in the parent's local timezone.
     */
    List<OfferingResponse> getAvailableOfferings(Long parentId);

    /**
     * Book an offering for a parent.
     *
     * Business rules enforced:
     *  1. Offering must be ACTIVE
     *  2. Parent must not have already booked this offering
     *  3. No session in this offering may overlap with any session
     *     in the parent's existing confirmed bookings
     *  4. Offering must not be at capacity
     *  5. Concurrent booking attempts must be serialised correctly
     */
    BookingResponse bookOffering(Long parentId, BookOfferingRequest request);

    /**
     * Get all CONFIRMED bookings for a parent.
     * Session times are rendered in the parent's local timezone.
     */
    List<BookingResponse> getParentBookings(Long parentId);
}

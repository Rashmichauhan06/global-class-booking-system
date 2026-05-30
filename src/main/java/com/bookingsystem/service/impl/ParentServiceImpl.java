package com.bookingsystem.service.impl;

import com.bookingsystem.dto.request.BookOfferingRequest;
import com.bookingsystem.dto.request.CreateParentRequest;
import com.bookingsystem.dto.response.Responses.*;
import com.bookingsystem.entity.*;
import com.bookingsystem.exception.Exceptions;
import com.bookingsystem.repository.*;
import com.bookingsystem.service.ParentService;
import com.bookingsystem.util.ResponseMapper;
import com.bookingsystem.util.TimezoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;

    // ── Parent Registration ───────────────────────────────────────────────────

    @Override
    @Transactional
    public ParentResponse createParent(CreateParentRequest request) {
        TimezoneUtil.validateAndGet(request.timezone());

        if (parentRepository.existsByEmail(request.email())) {
            throw new Exceptions.DuplicateBookingException(
                    "A parent with email '" + request.email() + "' already exists.");
        }

        Parent parent = Parent.builder()
                .name(request.name())
                .email(request.email())
                .timezone(request.timezone())
                .build();

        return ResponseMapper.toParentResponse(parentRepository.save(parent));
    }

    // ── Browse Offerings ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OfferingResponse> getAvailableOfferings(Long parentId) {
        Parent parent = findParent(parentId);

        return offeringRepository.findAvailableOfferings(Instant.now())
                .stream()
                .map(o -> {
                    o.setSessions(sessionRepository.findByOfferingIdOrderByStartTimeUtcAsc(o.getId()));
                    // Display session times in the parent's local timezone
                    return ResponseMapper.toOfferingResponse(o, parent.getTimezone());
                })
                .toList();
    }

    // ── Book Offering ─────────────────────────────────────────────────────────


    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingResponse bookOffering(Long parentId, BookOfferingRequest request) {
        Parent parent = findParent(parentId);

        // 1. Fetch and lock the offering
        Offering offering = offeringRepository.findById(request.offeringId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException(
                        "Offering", request.offeringId()));

        // 2. Offering must be ACTIVE
        if (offering.getStatus() != OfferingStatus.ACTIVE) {
            throw new Exceptions.OfferingNotActiveException(offering.getId());
        }

        // 3. Offering must have at least one future session
        List<Session> targetSessions = sessionRepository
                .findByOfferingIdOrderByStartTimeUtcAsc(offering.getId())
                .stream()
                .filter(s -> s.getStatus() == SessionStatus.SCHEDULED
                          && s.getStartTimeUtc().isAfter(Instant.now()))
                .toList();

        if (targetSessions.isEmpty()) {
            throw new Exceptions.InvalidSessionTimeException(
                    "Offering " + offering.getId() + " has no upcoming sessions.");
        }

        // 4. Duplicate booking check (parent already booked this offering)
        if (bookingRepository.existsByParentIdAndOfferingId(parentId, offering.getId())) {
            throw new Exceptions.DuplicateBookingException(
                    "You have already booked offering: " + offering.getTitle());
        }

        // 5. Capacity check
        long confirmedCount = bookingRepository.countConfirmedBookings(offering.getId());
        if (confirmedCount >= offering.getMaxStudents()) {
            throw new Exceptions.OfferingFullException(
                    "Offering '" + offering.getTitle() + "' is full (" +
                    offering.getMaxStudents() + "/" + offering.getMaxStudents() + " students).");
        }

        // 6. Schedule conflict check (the most critical rule)
        //    Single optimised DB query — avoids loading all booked sessions into memory.
        long conflicts = sessionRepository.countConflictingSessions(parentId, offering.getId());
        if (conflicts > 0) {
            log.warn("Parent {} attempted to book offering {} but has {} conflicting sessions",
                    parentId, offering.getId(), conflicts);
            throw new Exceptions.ScheduleConflictException(
                    "Cannot book '" + offering.getTitle() + "': one or more sessions overlap " +
                    "with your existing bookings. Please review your schedule.");
        }

        // 7. All checks passed — create the booking
        Booking booking = Booking.builder()
                .parent(parent)
                .offering(offering)
                .build();

        Booking saved = bookingRepository.save(booking);
        log.info("Parent {} booked offering {} (booking id: {})",
                parentId, offering.getId(), saved.getId());

        // Re-load sessions for the response
        offering.setSessions(sessionRepository.findByOfferingIdOrderByStartTimeUtcAsc(offering.getId()));

        return ResponseMapper.toBookingResponse(saved, parent.getTimezone());
    }

    // ── Parent Bookings ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getParentBookings(Long parentId) {
        Parent parent = findParent(parentId);

        return bookingRepository
                .findByParentIdAndStatus(parentId, BookingStatus.CONFIRMED)
                .stream()
                .map(b -> {
                    b.getOffering().setSessions(
                        sessionRepository.findByOfferingIdOrderByStartTimeUtcAsc(b.getOffering().getId())
                    );
                    return ResponseMapper.toBookingResponse(b, parent.getTimezone());
                })
                .toList();
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Parent findParent(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Parent", id));
    }
}

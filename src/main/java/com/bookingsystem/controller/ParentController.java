package com.bookingsystem.controller;

import com.bookingsystem.dto.request.BookOfferingRequest;
import com.bookingsystem.dto.request.CreateParentRequest;
import com.bookingsystem.dto.response.Responses.*;
import com.bookingsystem.service.ParentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Parent/Student-facing APIs.
 *
 * Base path: /api/v1/parents
 */
@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    // POST /api/v1/parents
    @PostMapping
    public ResponseEntity<ParentResponse> createParent(
            @Valid @RequestBody CreateParentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parentService.createParent(request));
    }

    // GET /api/v1/parents/{parentId}/offerings/available
    @GetMapping("/{parentId}/offerings/available")
    public ResponseEntity<List<OfferingResponse>> getAvailableOfferings(
            @PathVariable Long parentId) {
        return ResponseEntity.ok(parentService.getAvailableOfferings(parentId));
    }

    // POST /api/v1/parents/{parentId}/bookings
    @PostMapping("/{parentId}/bookings")
    public ResponseEntity<BookingResponse> bookOffering(
            @PathVariable Long parentId,
            @Valid @RequestBody BookOfferingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parentService.bookOffering(parentId, request));
    }

    // GET /api/v1/parents/{parentId}/bookings
    @GetMapping("/{parentId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookings(
            @PathVariable Long parentId) {
        return ResponseEntity.ok(parentService.getParentBookings(parentId));
    }
}

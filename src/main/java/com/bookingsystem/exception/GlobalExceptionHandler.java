package com.bookingsystem.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Structured error envelope ─────────────────────────────────────────────

    public record ErrorResponse(
            int status,
            String error,
            String message,
            String path,
            Instant timestamp
    ) {}

    public record ValidationErrorResponse(
            int status,
            String error,
            Map<String, String> fieldErrors,
            String path,
            Instant timestamp
    ) {}

    // ── Handlers ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exceptions.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            Exceptions.ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(Exceptions.ScheduleConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            Exceptions.ScheduleConflictException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(Exceptions.DuplicateBookingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBooking(
            Exceptions.DuplicateBookingException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(Exceptions.OfferingFullException.class)
    public ResponseEntity<ErrorResponse> handleOfferingFull(
            Exceptions.OfferingFullException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exceptions.InvalidTimezoneException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimezone(
            Exceptions.InvalidTimezoneException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exceptions.InvalidSessionTimeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTime(
            Exceptions.InvalidSessionTimeException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exceptions.OfferingNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleNotActive(
            Exceptions.OfferingNotActiveException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        var body = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                fieldErrors,
                path(request),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        // Duplicate key on (parent_id, offering_id) unique constraint
        String message = ex.getMessage() != null && ex.getMessage().contains("uk_bookings_parent_offering")
                ? "You have already booked this offering."
                : "A data conflict occurred. Please try again.";
        return build(HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        var body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                path(request),
                Instant.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    private String path(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}

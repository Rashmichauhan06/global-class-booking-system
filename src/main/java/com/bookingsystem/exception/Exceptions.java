package com.bookingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public final class Exceptions {

    private Exceptions() {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String resource, Long id) {
            super(resource + " not found with id: " + id);
        }
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class ScheduleConflictException extends RuntimeException {
        public ScheduleConflictException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateBookingException extends RuntimeException {
        public DuplicateBookingException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class OfferingFullException extends RuntimeException {
        public OfferingFullException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidTimezoneException extends RuntimeException {
        public InvalidTimezoneException(String timezone) {
            super("Invalid IANA timezone: '" + timezone + "'");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidSessionTimeException extends RuntimeException {
        public InvalidSessionTimeException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class OfferingNotActiveException extends RuntimeException {
        public OfferingNotActiveException(Long offeringId) {
            super("Offering " + offeringId + " is not active and cannot be booked.");
        }
    }
}

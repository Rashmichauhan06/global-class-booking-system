package com.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AddSessionRequest(

        /**
         * Local date-time in the TEACHER's timezone.
         * Format: "2025-06-07T18:00:00"
         * The service converts this to UTC before storing.
         */
        @NotNull(message = "Start time is required")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        LocalDateTime endTime
) {}

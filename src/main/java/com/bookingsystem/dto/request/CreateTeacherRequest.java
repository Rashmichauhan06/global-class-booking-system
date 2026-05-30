package com.bookingsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeacherRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        @Size(max = 150)
        String email,

        /**
         * IANA timezone, e.g. "America/New_York", "Asia/Tokyo".
         * Validated in the service layer against Java's ZoneId.
         */
        @NotBlank(message = "Timezone is required (IANA format, e.g. America/New_York)")
        String timezone
) {}

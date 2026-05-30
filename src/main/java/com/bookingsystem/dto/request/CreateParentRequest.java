package com.bookingsystem.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateParentRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Timezone is required (IANA format, e.g. Asia/Kolkata)")
        String timezone
) {}

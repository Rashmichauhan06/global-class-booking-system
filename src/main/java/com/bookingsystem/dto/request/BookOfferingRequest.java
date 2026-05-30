package com.bookingsystem.dto.request;

import jakarta.validation.constraints.NotNull;

public record BookOfferingRequest(

        @NotNull(message = "Offering ID is required")
        Long offeringId
) {}

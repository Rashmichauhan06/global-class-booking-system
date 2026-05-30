package com.bookingsystem.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(

        @NotBlank(message = "Course title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description) {
}

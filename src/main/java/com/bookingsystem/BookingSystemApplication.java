package com.bookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BookingSystemApplication {

    public static void main(String[] args) {
        // Force JVM to use UTC — critical for correct Instant <-> LocalDateTime conversions
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(BookingSystemApplication.class, args);
    }
}

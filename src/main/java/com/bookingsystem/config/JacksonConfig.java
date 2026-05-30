package com.bookingsystem.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // Instants serialised as ISO-8601 strings, not epoch milliseconds
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Enforce UTC for any date serialisation
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        return mapper;
    }
}

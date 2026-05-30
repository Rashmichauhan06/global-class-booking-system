package com.bookingsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables @CreatedDate and @LastModifiedDate on BaseEntity.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}

-- ============================================================
-- V1: Initial Schema — Global Class Offering Booking System
-- All timestamps stored in UTC
-- ============================================================

-- Teachers
CREATE TABLE teachers (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL,
    timezone    VARCHAR(50)     NOT NULL COMMENT 'IANA timezone, e.g. America/New_York',
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_teachers_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Parents / Students
CREATE TABLE parents (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL,
    timezone    VARCHAR(50)     NOT NULL COMMENT 'IANA timezone, e.g. Asia/Kolkata',
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_parents_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Courses (subject/topic)
CREATE TABLE courses (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    title       VARCHAR(200)    NOT NULL,
    description TEXT,
    created_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Offerings / Sections  (a schedulable version of a course)
CREATE TABLE offerings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    course_id       BIGINT          NOT NULL,
    teacher_id      BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL  COMMENT 'e.g. Saturday Batch, Evening Batch',
    description     TEXT,
    max_students    INT             NOT NULL DEFAULT 30,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                                    COMMENT 'ACTIVE | CANCELLED | COMPLETED',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_offerings_course  FOREIGN KEY (course_id)  REFERENCES courses(id),
    CONSTRAINT fk_offerings_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id),

    INDEX idx_offerings_teacher   (teacher_id),
    INDEX idx_offerings_course    (course_id),
    INDEX idx_offerings_status    (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Sessions (actual meeting slots inside an offering — stored in UTC)
CREATE TABLE sessions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    offering_id     BIGINT          NOT NULL,
    teacher_id      BIGINT          NOT NULL,
    start_time_utc  DATETIME(6)     NOT NULL COMMENT 'Always UTC',
    end_time_utc    DATETIME(6)     NOT NULL COMMENT 'Always UTC',
    status          VARCHAR(20)     NOT NULL DEFAULT 'SCHEDULED'
                                    COMMENT 'SCHEDULED | CANCELLED | COMPLETED',
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_sessions_offering FOREIGN KEY (offering_id) REFERENCES offerings(id),
    CONSTRAINT fk_sessions_teacher  FOREIGN KEY (teacher_id)  REFERENCES teachers(id),
    CONSTRAINT chk_session_times    CHECK (end_time_utc > start_time_utc),

    INDEX idx_sessions_offering     (offering_id),
    INDEX idx_sessions_teacher      (teacher_id),
    -- Critical index for overlap queries
    INDEX idx_sessions_time_range   (start_time_utc, end_time_utc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Bookings (parent books a full offering)
CREATE TABLE bookings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT          NOT NULL,
    offering_id     BIGINT          NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'CONFIRMED'
                                    COMMENT 'CONFIRMED | CANCELLED',
    booked_at       DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_bookings_parent   FOREIGN KEY (parent_id)   REFERENCES parents(id),
    CONSTRAINT fk_bookings_offering FOREIGN KEY (offering_id) REFERENCES offerings(id),

    -- A parent can only book a given offering once (active booking)
    UNIQUE KEY uk_bookings_parent_offering (parent_id, offering_id),

    INDEX idx_bookings_parent       (parent_id),
    INDEX idx_bookings_offering     (offering_id),
    INDEX idx_bookings_status       (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

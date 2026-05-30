# Global Class Booking System

A RESTful backend service for managing and booking fitness/wellness classes across multiple timezones. Built with Spring Boot 3, it supports class scheduling, user bookings, concurrency-safe seat management, and timezone-aware time handling.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Environment Variables](#environment-variables)
- [Running the Application Locally](#running-the-application-locally)
- [API Documentation](#api-documentation)
- [Database Schema Overview](#database-schema-overview)
- [Concurrency Handling](#concurrency-handling)
- [Timezone Handling](#timezone-handling)
- [Assumptions Made](#assumptions-made)

---

## Project Overview

The Global Class Booking System allows users to:

- Browse available fitness/wellness classes with scheduling details
- Book a spot in a class (with capacity enforcement)
- Cancel an existing booking
- View upcoming classes in their local timezone

The system is designed to handle concurrent booking requests safely, preventing overbooking when multiple users attempt to book the last available slot simultaneously.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.x |
| Web | Spring MVC (REST) |
| Persistence | Spring Data JPA (Hibernate) |
| Database | MySQL 8+ |
| Migrations | Flyway |
| Validation | Spring Boot Validation (Jakarta Bean Validation) |
| Boilerplate Reduction | Lombok |
| Build Tool | Maven (via Maven Wrapper) |
| Dev Tools | Spring Boot DevTools |

---

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 21** — [Download](https://adoptium.net/)
- **Maven 3.9+** — or use the included `./mvnw` wrapper (no separate installation needed)
- **MySQL 8.0+** — running locally or via Docker
- **Git**

---

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/global-class-booking.git
cd global-class-booking
```

### 2. Set Up the Database

Create a MySQL database for the application:

```sql
CREATE DATABASE global_class_booking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'booking_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON global_class_booking.* TO 'booking_user'@'localhost';
FLUSH PRIVILEGES;
```

> Flyway will automatically run all migration scripts from `src/main/resources/db/migration/` on startup, creating and seeding the schema.

### 3. Configure Environment

Copy the example properties and fill in your values:

```bash
cp src/main/resources/application.properties src/main/resources/application-local.properties
```

Edit `application-local.properties` with your database credentials (see [Environment Variables](#environment-variables) below).

### 4. Build the Project

```bash
./mvnw clean install -DskipTests
```

## Environment Variables

The application reads the following configuration from `application.properties` (or environment variables / a `.env` file when deploying):

| Property | Environment Variable | Description | Example |
|---|---|---|---|
| `spring.datasource.url` | `DB_URL` | JDBC URL for MySQL | `jdbc:mysql://localhost:3306/global_class_booking?useSSL=false&serverTimezone=UTC` |
| `spring.datasource.username` | `DB_USERNAME` | Database username | `booking_user` |
| `spring.datasource.password` | `DB_PASSWORD` | Database password | `your_password` |
| `spring.jpa.hibernate.ddl-auto` | — | DDL strategy (use `validate` in prod) | `validate` |
| `spring.flyway.enabled` | — | Enable/disable Flyway migrations | `true` |
| `server.port` | `SERVER_PORT` | HTTP port the app listens on | `8080` |
| `app.default-timezone` | `APP_DEFAULT_TIMEZONE` | Server-side default timezone | `UTC` |

**Minimal `application.properties` example:**

```properties
spring.application.name=global-class-booking

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/global_class_booking?useSSL=false&serverTimezone=UTC
spring.datasource.username=booking_user
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Server
server.port=8080

# Timezone
app.default-timezone=UTC
```

> Never commit credentials to version control. Use environment variables or a secrets manager in production.

---

## Running the Application Locally

### Option A — Maven Wrapper (Recommended)

```bash
./mvnw spring-boot:run
```

### Option B — Packaged JAR

```bash
./mvnw clean package -DskipTests
java -jar target/global-class-booking-0.0.1-SNAPSHOT.jar
```

### Option C — Docker Compose (MySQL + App)

```yaml
# docker-compose.yml (create at project root)
version: '3.9'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: global_class_booking
      MYSQL_USER: booking_user
      MYSQL_PASSWORD: your_password
      MYSQL_ROOT_PASSWORD: root_password
    ports:
      - "3306:3306"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:mysql://mysql:3306/global_class_booking?useSSL=false&serverTimezone=UTC
      DB_USERNAME: booking_user
      DB_PASSWORD: your_password
    depends_on:
      - mysql
```

```bash
docker-compose up --build
```

Once started, the API is available at `http://localhost:8080`.

---

## API Documentation

All endpoints return JSON. Timestamps in request/response bodies use **ISO 8601 format with timezone offset** (e.g., `2025-06-15T10:00:00+05:30`).

### Base URL

```
http://localhost:8080/api/v1
```

---

### Classes

#### `GET /classes`
Retrieve all available upcoming classes.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `timezone` | String | No | IANA timezone name (e.g., `Asia/Kolkata`). Defaults to `UTC`. |
| `page` | int | No | Page number (0-indexed). Default: `0` |
| `size` | int | No | Page size. Default: `20` |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Morning Yoga",
      "instructor": "Priya Sharma",
      "scheduledAt": "2025-06-15T10:00:00+05:30",
      "durationMinutes": 60,
      "totalSlots": 20,
      "availableSlots": 5,
      "timezone": "Asia/Kolkata"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

#### `GET /classes/{id}`
Retrieve details of a single class.

**Response `200 OK`:** Single class object (same schema as above).

**Response `404 Not Found`:** Class does not exist.

---

### Bookings

#### `POST /bookings`
Book a slot in a class.

**Request Body:**
```json
{
  "classId": 1,
  "userId": 42,
  "clientTimezone": "Asia/Kolkata"
}
```

**Response `201 Created`:**
```json
{
  "bookingId": 101,
  "classId": 1,
  "className": "Morning Yoga",
  "userId": 42,
  "scheduledAt": "2025-06-15T10:00:00+05:30",
  "bookedAt": "2025-06-01T08:23:10+05:30",
  "status": "CONFIRMED"
}
```

**Response `409 Conflict`:** No slots available.

**Response `400 Bad Request`:** User already booked this class.

---

#### `DELETE /bookings/{bookingId}`
Cancel an existing booking.

**Response `200 OK`:**
```json
{
  "bookingId": 101,
  "status": "CANCELLED",
  "cancelledAt": "2025-06-01T09:00:00+05:30"
}
```

**Response `404 Not Found`:** Booking does not exist.

---

#### `GET /bookings/user/{userId}`
Retrieve all bookings for a specific user.

**Query Parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `timezone` | String | No | IANA timezone for response timestamps. Default: `UTC` |
| `status` | String | No | Filter by status: `CONFIRMED`, `CANCELLED` |

**Response `200 OK`:** Array of booking objects.

---

### Error Response Format

All errors follow a consistent structure:

```json
{
  "timestamp": "2025-06-01T08:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "No available slots for class ID 1",
  "path": "/api/v1/bookings"
}
```

---

## Database Schema Overview

Flyway manages schema versioning. Migration scripts live in `src/main/resources/db/migration/`.

### `classes`

Stores scheduled class information.

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Auto-incremented primary key |
| `name` | VARCHAR(255) | Class name |
| `instructor` | VARCHAR(255) | Instructor name |
| `scheduled_at` | DATETIME | Class start time, stored in **UTC** |
| `duration_minutes` | INT | Duration of the class |
| `total_slots` | INT | Maximum capacity |
| `available_slots` | INT | Remaining bookable slots (concurrency-controlled) |
| `created_at` | DATETIME | Record creation timestamp (UTC) |
| `updated_at` | DATETIME | Last update timestamp (UTC) |

---

### `users`

Stores user account information.

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Auto-incremented primary key |
| `name` | VARCHAR(255) | Full name |
| `email` | VARCHAR(255) UNIQUE | Email address |
| `preferred_timezone` | VARCHAR(100) | User's IANA timezone preference |
| `created_at` | DATETIME | Record creation timestamp (UTC) |

---

### `bookings`

Records each booking made by a user.

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Auto-incremented primary key |
| `user_id` | BIGINT FK | References `users.id` |
| `class_id` | BIGINT FK | References `classes.id` |
| `status` | ENUM | `CONFIRMED` or `CANCELLED` |
| `booked_at` | DATETIME | When booking was created (UTC) |
| `cancelled_at` | DATETIME | When booking was cancelled (UTC), nullable |

**Unique constraint:** `(user_id, class_id)` — prevents duplicate bookings.

---

## Concurrency Handling

Multiple users booking the last available slot simultaneously is the core concurrency challenge. The system handles this using **optimistic locking** at the JPA layer:

- The `classes` entity has a `@Version` field (managed by Hibernate).
- When two transactions read the same row, increment `available_slots`, and attempt to write back, only the first commit succeeds. The second transaction triggers an `OptimisticLockException`, which is caught and returned to the client as a `409 Conflict`.
- As an additional safeguard, a **database-level CHECK constraint** (`available_slots >= 0`) prevents negative slot counts from ever being persisted.
- For higher-throughput scenarios, the booking flow can optionally be switched to **pessimistic locking** (`SELECT ... FOR UPDATE`) by switching the repository query annotation — this serializes writes at the cost of some throughput.

This approach ensures that exactly the right number of seats are booked without race conditions, without requiring an external queue or distributed lock.

---

## Timezone Handling

All datetimes are stored in the database as **UTC**. Timezone conversion happens at the API boundary:

- **Inbound:** When a client submits a booking request, an optional `clientTimezone` field (IANA name, e.g., `America/New_York`) is accepted but the persisted timestamp is always normalized to UTC.
- **Outbound:** When returning class schedules or booking details, the API converts UTC timestamps to the requested timezone using the `timezone` query parameter (or the user's `preferred_timezone` profile setting, or UTC as the final fallback).
- Java's `java.time` package (`ZonedDateTime`, `ZoneId`) is used exclusively — the legacy `java.util.Date` and `java.util.Calendar` classes are avoided entirely.
- The MySQL JDBC connection string includes `serverTimezone=UTC` to ensure the driver and database communicate in UTC at all times.
- DST transitions are handled transparently by the `java.time` library; no manual offset arithmetic is performed in application code.

---

## Assumptions Made

1. **Authentication is out of scope.** The API accepts a `userId` in the request body directly without JWT/session validation. In production, an authentication filter would inject the authenticated user's ID.

2. **Classes are pre-seeded by an admin.** There is no admin API for creating classes in this version; classes are inserted via Flyway seed migrations.

3. **One booking per user per class.** A user cannot book the same class more than once. Re-booking after cancellation is not supported in v1.

4. **No waitlist.** When `available_slots` reaches 0, new booking attempts are immediately rejected.

5. **Soft cancellation.** Cancelling a booking sets the status to `CANCELLED` and increments `available_slots` back; the record is not deleted from the database.

6. **UTC as the canonical timezone.** The system treats UTC as the single source of truth for all stored times. Display timezone is a presentation concern only.

7. **MySQL 8.0+.** The Flyway dialect and JDBC driver are configured specifically for MySQL. Switching to PostgreSQL would require minor dependency and script changes.

8. **Single-region deployment.** Horizontal scaling and distributed caching (e.g., Redis-based locking) are not implemented in this version.

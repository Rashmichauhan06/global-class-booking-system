package com.bookingsystem.controller;

import com.bookingsystem.dto.request.AddSessionRequest;
import com.bookingsystem.dto.request.CreateCourseRequest;
import com.bookingsystem.dto.request.CreateOfferingRequest;
import com.bookingsystem.dto.request.CreateTeacherRequest;
import com.bookingsystem.dto.response.Responses.*;
import com.bookingsystem.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Teacher-facing APIs.
 *
 * Base path: /api/v1/teachers
 */
@RestController
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    // POST /api/v1/teachers
    @PostMapping
    public ResponseEntity<TeacherResponse> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.createTeacher(request));
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(
            @Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.createCourse(request));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourse(
            @PathVariable Long courseId) {
        return ResponseEntity.ok(teacherService.getCourseById(courseId));
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(teacherService.getAllCourses());
    }
    // POST /api/v1/teachers/{teacherId}/offerings
    @PostMapping("/{teacherId}/offerings")
    public ResponseEntity<OfferingResponse> createOffering(
            @PathVariable Long teacherId,
            @Valid @RequestBody CreateOfferingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.createOffering(teacherId, request));
    }

    // POST /api/v1/teachers/{teacherId}/offerings/{offeringId}/sessions
    @PostMapping("/{teacherId}/offerings/{offeringId}/sessions")
    public ResponseEntity<SessionResponse> addSession(
            @PathVariable Long teacherId,
            @PathVariable Long offeringId,
            @Valid @RequestBody AddSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teacherService.addSession(teacherId, offeringId, request));
    }

    // GET /api/v1/teachers/{teacherId}/offerings
    @GetMapping("/{teacherId}/offerings")
    public ResponseEntity<List<OfferingResponse>> getOfferings(
            @PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getTeacherOfferings(teacherId));
    }

    // GET /api/v1/teachers/{teacherId}/sessions/upcoming
    @GetMapping("/{teacherId}/sessions/upcoming")
    public ResponseEntity<List<SessionResponse>> getUpcomingSessions(
            @PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherService.getUpcomingSessions(teacherId));
    }
}

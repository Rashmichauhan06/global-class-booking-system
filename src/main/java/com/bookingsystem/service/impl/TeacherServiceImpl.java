package com.bookingsystem.service.impl;

import com.bookingsystem.dto.request.AddSessionRequest;
import com.bookingsystem.dto.request.CreateCourseRequest;
import com.bookingsystem.dto.request.CreateOfferingRequest;
import com.bookingsystem.dto.request.CreateTeacherRequest;
import com.bookingsystem.dto.response.Responses.*;
import com.bookingsystem.entity.*;
import com.bookingsystem.exception.Exceptions;
import com.bookingsystem.repository.*;
import com.bookingsystem.service.TeacherService;
import com.bookingsystem.util.ResponseMapper;
import com.bookingsystem.util.TimezoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final OfferingRepository offeringRepository;
    private final SessionRepository sessionRepository;

    // ── Teacher Registration ──────────────────────────────────────────────────

    @Override
    @Transactional
    public TeacherResponse createTeacher(CreateTeacherRequest request) {
        TimezoneUtil.validateAndGet(request.timezone()); // fail fast

        if (teacherRepository.existsByEmail(request.email())) {
            throw new Exceptions.DuplicateBookingException(
                    "A teacher with email '" + request.email() + "' already exists.");
        }

        Teacher teacher = Teacher.builder()
                .name(request.name())
                .email(request.email())
                .timezone(request.timezone())
                .build();

        return ResponseMapper.toTeacherResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CreateCourseRequest request) {
        if (courseRepository.existsByTitleIgnoreCase(request.title())) {
            throw new Exceptions.DuplicateBookingException(
                    "A course with the title '" + request.title() + "' already exists.");
        }

        Course course = Course.builder()
                .title(request.title().trim())
                .description(request.description())
                .build();

        return ResponseMapper.toCourseResponse(courseRepository.save(course));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException(
                        "Course not found with id: " + courseId));
        return ResponseMapper.toCourseResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(ResponseMapper::toCourseResponse)
                .toList();
    }

    // ── Offerings ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OfferingResponse createOffering(Long teacherId, CreateOfferingRequest request) {
        Teacher teacher = findTeacher(teacherId);
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Course", request.courseId()));

        Offering offering = Offering.builder()
                .teacher(teacher)
                .course(course)
                .title(request.title())
                .description(request.description())
                .maxStudents(request.maxStudents() != null ? request.maxStudents() : 30)
                .build();

        Offering saved = offeringRepository.save(offering);
        log.info("Teacher {} created offering {} ({})", teacherId, saved.getId(), saved.getTitle());

        // Return with teacher's timezone for display
        return ResponseMapper.toOfferingResponse(saved, teacher.getTimezone());
    }

    // ── Sessions ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SessionResponse addSession(Long teacherId, Long offeringId, AddSessionRequest request) {
        Teacher teacher = findTeacher(teacherId);
        Offering offering = findOffering(offeringId);

        // Ownership check
        if (!offering.getTeacher().getId().equals(teacherId)) {
            throw new Exceptions.ResourceNotFoundException(
                    "Offering " + offeringId + " does not belong to teacher " + teacherId);
        }
        if (offering.getStatus() != OfferingStatus.ACTIVE) {
            throw new Exceptions.OfferingNotActiveException(offeringId);
        }

        // Validate times
        if (!request.endTime().isAfter(request.startTime())) {
            throw new Exceptions.InvalidSessionTimeException(
                    "Session end time must be after start time.");
        }
        if (request.startTime().isBefore(java.time.LocalDateTime.now())) {
            throw new Exceptions.InvalidSessionTimeException(
                    "Session start time must be in the future.");
        }

        // Convert teacher's local time → UTC
        Instant startUtc = TimezoneUtil.toUtc(request.startTime(), teacher.getTimezone());
        Instant endUtc   = TimezoneUtil.toUtc(request.endTime(),   teacher.getTimezone());

        Session session = Session.builder()
                .offering(offering)
                .teacher(teacher)
                .startTimeUtc(startUtc)
                .endTimeUtc(endUtc)
                .build();

        Session saved = sessionRepository.save(session);
        log.info("Teacher {} added session {} to offering {}", teacherId, saved.getId(), offeringId);

        // Display times back in teacher's timezone
        return ResponseMapper.toSessionResponse(saved, teacher.getTimezone());
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OfferingResponse> getTeacherOfferings(Long teacherId) {
        Teacher teacher = findTeacher(teacherId);

        return offeringRepository
                .findByTeacherIdAndStatus(teacherId, OfferingStatus.ACTIVE)
                .stream()
                .map(o -> {
                    // Pre-load sessions (avoids lazy-load in mapper)
                    o.setSessions(sessionRepository.findByOfferingIdOrderByStartTimeUtcAsc(o.getId()));
                    return ResponseMapper.toOfferingResponse(o, teacher.getTimezone());
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getUpcomingSessions(Long teacherId) {
        Teacher teacher = findTeacher(teacherId);
        return sessionRepository
                .findUpcomingByTeacher(teacherId, Instant.now())
                .stream()
                .map(s -> ResponseMapper.toSessionResponse(s, teacher.getTimezone()))
                .toList();
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private Teacher findTeacher(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Teacher", id));
    }

    private Offering findOffering(Long id) {
        return offeringRepository.findById(id)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Offering", id));
    }
}

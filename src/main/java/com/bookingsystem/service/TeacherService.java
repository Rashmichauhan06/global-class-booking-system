package com.bookingsystem.service;

import com.bookingsystem.dto.request.AddSessionRequest;
import com.bookingsystem.dto.request.CreateCourseRequest;
import com.bookingsystem.dto.request.CreateOfferingRequest;
import com.bookingsystem.dto.request.CreateTeacherRequest;
import com.bookingsystem.dto.response.Responses.*;

import java.util.List;

public interface TeacherService {

    TeacherResponse createTeacher(CreateTeacherRequest request);

    /**
     * Create a new offering under the teacher's account.
     */
    CourseResponse createCourse(CreateCourseRequest request);

    CourseResponse getCourseById(Long courseId);

    List<CourseResponse> getAllCourses();
    OfferingResponse createOffering(Long teacherId, CreateOfferingRequest request);

    /**
     * Add a session to an existing offering.
     * The session's startTime/endTime are provided in the teacher's timezone
     * and stored as UTC.
     */
    SessionResponse addSession(Long teacherId, Long offeringId, AddSessionRequest request);

    /**
     * Get all ACTIVE offerings for a teacher, with sessions displayed in the
     * teacher's own timezone.
     */
    List<OfferingResponse> getTeacherOfferings(Long teacherId);

    /**
     * Get upcoming sessions for a teacher (all their offerings combined).
     */
    List<SessionResponse> getUpcomingSessions(Long teacherId);
}

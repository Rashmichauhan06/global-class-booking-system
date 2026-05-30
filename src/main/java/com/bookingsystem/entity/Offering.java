package com.bookingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offerings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offering extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    /**
     * Human-readable label, e.g. "Saturday Batch", "Summer Camp Week 1"
     */
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_students", nullable = false)
    @Builder.Default
    private Integer maxStudents = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OfferingStatus status = OfferingStatus.ACTIVE;

    @OneToMany(mappedBy = "offering", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private List<Session> sessions = new ArrayList<>();
    public void setSessions(List<Session> sessions) {
        this.sessions.clear();

        if (sessions != null) {
            this.sessions.addAll(sessions);
        }
    }

    @OneToMany(mappedBy = "offering", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();
}

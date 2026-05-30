package com.bookingsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teachers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * IANA timezone identifier (e.g. "America/New_York").
     * Teachers create sessions in this timezone — we convert to UTC for storage.
     */
    @Column(nullable = false, length = 50)
    private String timezone;

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Offering> offerings = new ArrayList<>();
}

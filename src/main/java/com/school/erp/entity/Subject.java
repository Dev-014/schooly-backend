package com.school.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "code")
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    private String type = "THEORY";

    @Column(name = "credits")
    private Integer credits = 3;

    @Column(name = "grade_level")
    private String gradeLevel;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
}
